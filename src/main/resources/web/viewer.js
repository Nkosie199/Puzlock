import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

// Song et al.'s colour order for the first pieces; the remainder is always green.
const PIECE_COLOURS = ['#e53935', '#1e88e5', '#fdd835', '#fb8c00', '#8e24aa', '#00acc1', '#6d4c41',
  '#d81b60', '#3949ab', '#c0ca33', '#5e35b1', '#00897b', '#f4511e', '#546e7a', '#7cb342', '#ffb300'];
const REMAINDER_COLOUR = '#43a047';
const INPUT_COLOUR = '#9e9e9e';
const VOXEL_GAP = 0.94;
const EASING = 0.18;
const REMOVED_OPACITY = 0.2;
const DIRECTIONS = {
  right: [1, 0, 0], left: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0], forward: [0, 0, 1], backward: [0, 0, -1],
};

const el = (id) => document.getElementById(id);
const view = createView(el('scene'));
const state = { puzzle: null, pieces: [], removed: 0, explode: 0 };

init();

async function init() {
  await loadSamples();
  el('generate-form').addEventListener('submit', (event) => { event.preventDefault(); generate(); });
  el('sample').addEventListener('change', () => { if (el('sample').value) el('file').value = ''; });
  el('file').addEventListener('change', () => { if (el('file').files.length) el('sample').value = ''; });
  el('explode').addEventListener('input', () => { state.explode = Number(el('explode').value); updateTargets(); });
  el('step-forward').addEventListener('click', () => setRemoved(state.removed + 1));
  el('step-back').addEventListener('click', () => setRemoved(state.removed - 1));
}

async function loadSamples() {
  const names = await (await fetch('/api/samples')).json();
  for (const name of names) {
    el('sample').append(new Option(name, name));
  }
  if (names.includes('cube4.grid')) el('sample').value = 'cube4.grid';
}

async function generate() {
  const sample = el('sample').value;
  const file = el('file').files[0];
  if (!sample && !file) {
    showStatus('Choose a sample or a file first.', 'error');
    return;
  }
  const params = new URLSearchParams({
    pieces: el('pieces').value, resolution: el('resolution').value, seed: el('seed').value,
  });
  if (sample) params.set('sample', sample); else params.set('name', file.name);
  el('generate').disabled = true;
  showStatus('Generating…');
  try {
    const response = await fetch('/api/generate?' + params, { method: 'POST', body: sample ? '' : file });
    if (!response.ok) throw new Error(await response.text());
    show(await response.json());
  } catch (error) {
    showStatus('Request failed: ' + error.message, 'error');
  } finally {
    el('generate').disabled = false;
  }
}

function show(result) {
  state.puzzle = result;
  state.removed = 0;
  const note = result.note ? `\nNote: ${result.note}` : '';
  if (!result.ok) {
    showStatus(`Can't make this puzzle: ${result.error}${note}`, 'error');
    state.pieces = result.inputVoxels ? buildPieces(result.shape, [{ voxels: result.inputVoxels, colour: INPUT_COLOUR }]) : [];
    el('controls').hidden = true;
    return;
  }
  const seconds = (result.elapsedMs / 1000).toFixed(1);
  showStatus(`${result.verification}.\n${result.pieces.length} pieces from ${result.voxelCount} voxels `
    + `(${result.shape.join('×')} grid) in ${seconds} s.${note}`, 'ok');
  state.pieces = buildPieces(result.shape, result.pieces.map((piece, index) => ({
    ...piece, colour: piece.direction ? PIECE_COLOURS[index % PIECE_COLOURS.length] : REMAINDER_COLOUR,
  })));
  el('controls').hidden = false;
  renderPieceList(result);
  setRemoved(0);
}

function showStatus(text, kind = '') {
  el('status').textContent = text;
  el('status').className = 'status ' + kind;
}

function buildPieces(shape, pieceData) {
  view.clear();
  const centre = new THREE.Vector3(shape[0] / 2, shape[1] / 2, shape[2] / 2);
  const box = new THREE.BoxGeometry(VOXEL_GAP, VOXEL_GAP, VOXEL_GAP);
  const pieces = pieceData.map((data) => {
    const material = new THREE.MeshStandardMaterial({ color: data.colour, roughness: 0.55, metalness: 0.05, transparent: true });
    const mesh = new THREE.InstancedMesh(box, material, data.voxels.length);
    const matrix = new THREE.Matrix4();
    const centroid = new THREE.Vector3();
    data.voxels.forEach(([x, y, z], i) => {
      mesh.setMatrixAt(i, matrix.makeTranslation(x + 0.5 - centre.x, y + 0.5 - centre.y, z + 0.5 - centre.z));
      centroid.add(new THREE.Vector3(x + 0.5, y + 0.5, z + 0.5));
    });
    centroid.divideScalar(Math.max(1, data.voxels.length)).sub(centre);
    const group = new THREE.Group();
    group.add(mesh);
    view.add(group);
    return { group, material, centroid, direction: data.direction, target: new THREE.Vector3(), visible: true };
  });
  view.frame(Math.max(...shape));
  return pieces;
}

function setRemoved(count) {
  const extracted = state.pieces.length - 1;
  state.removed = Math.max(0, Math.min(extracted, count));
  el('step-back').disabled = state.removed === 0;
  el('step-forward').disabled = state.removed === extracted;
  el('stage').textContent = state.removed === 0
    ? 'Assembled. Only piece 1 (the key) can move.'
    : state.removed === extracted
      ? 'Fully taken apart.'
      : `${state.removed} removed. Piece ${state.removed + 1} is now the only piece that can move.`;
  document.querySelectorAll('#piece-list li').forEach((item, index) => item.classList.toggle('removed', index < state.removed));
  updateTargets();
}

function updateTargets() {
  const travel = state.puzzle ? Math.max(...state.puzzle.shape) * 1.6 : 0;
  state.pieces.forEach((piece, index) => {
    const removed = index < state.removed && piece.direction;
    piece.target.copy(piece.centroid).multiplyScalar(state.explode);
    if (removed) {
      piece.target.add(new THREE.Vector3(...DIRECTIONS[piece.direction]).multiplyScalar(travel));
    }
    piece.material.opacity = removed ? REMOVED_OPACITY : 1;
    piece.material.depthWrite = !removed;
  });
}

function renderPieceList(result) {
  const list = el('piece-list');
  list.replaceChildren();
  result.pieces.forEach((piece, index) => {
    const item = document.createElement('li');
    const swatch = document.createElement('span');
    swatch.className = 'swatch';
    swatch.title = 'Show or hide this piece';
    swatch.style.background = state.pieces[index].material.color.getStyle();
    swatch.addEventListener('click', () => {
      const shown = (state.pieces[index].visible = !state.pieces[index].visible);
      state.pieces[index].group.visible = shown;
      swatch.classList.toggle('hidden-piece', !shown);
    });
    const label = document.createElement('span');
    label.textContent = `Piece ${piece.number}: ${piece.size} voxels, ` + (piece.direction ? `out ${piece.direction}` : 'last piece');
    const links = document.createElement('span');
    for (const format of ['stl', 'grid']) {
      const link = document.createElement('a');
      const path = `/api/results/${result.resultId}/piece-${piece.number}.${format}`;
      link.href = path;
      link.textContent = format.toUpperCase();
      link.download = `piece-${piece.number}.${format}`;
      if (format === 'stl') {
        link.addEventListener('click', () => { link.href = `${path}?clearance=${encodeURIComponent(el('clearance').value)}`; });
      }
      links.append(link);
    }
    item.append(swatch, label, links);
    list.append(item);
  });
}

function createView(container) {
  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setPixelRatio(window.devicePixelRatio);
  container.append(renderer.domElement);
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(40, 1, 0.1, 2000);
  const controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  scene.add(new THREE.HemisphereLight(0xffffff, 0x445566, 1.6));
  const sun = new THREE.DirectionalLight(0xffffff, 1.4);
  sun.position.set(3, 5, 4);
  scene.add(sun);
  const content = new THREE.Group();
  scene.add(content);

  const resize = () => {
    const { clientWidth: width, clientHeight: height } = container;
    renderer.setSize(width, height, false);
    camera.aspect = width / Math.max(1, height);
    camera.updateProjectionMatrix();
  };
  new ResizeObserver(resize).observe(container);
  resize();

  renderer.setAnimationLoop(() => {
    for (const piece of state.pieces) piece.group.position.lerp(piece.target, EASING);
    controls.update();
    renderer.render(scene, camera);
  });

  return {
    add: (object) => content.add(object),
    clear: () => {
      for (const child of [...content.children]) {
        child.traverse((node) => { if (node.isMesh) node.material.dispose(); });
        content.remove(child);
      }
    },
    frame: (size) => {
      camera.position.set(size * 1.6, size * 1.3, size * 2.0);
      controls.target.set(0, 0, 0);
      controls.update();
    },
  };
}
