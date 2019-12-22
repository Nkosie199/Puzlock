# Puzlock
Implementation of an algorithm which uses 3D arrays to generate recursively interlocking 3D puzzle pieces

To Do:
1) an anchor voxel must be determined for EACH initially blocked direction, there is therefore a SET of anchor voxels, one for each blocked direction. The anchor voxels should NOT be part of the key piece.
2) when expanding the key piece remember to iterate over the 3 steps until there is an appropriate number of voxels.
