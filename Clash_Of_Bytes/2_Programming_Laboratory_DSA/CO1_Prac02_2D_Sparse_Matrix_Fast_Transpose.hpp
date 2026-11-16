#ifndef CO1_PRAC02_2D_SPARSE_MATRIX_FAST_TRANSPOSE_HPP
#define CO1_PRAC02_2D_SPARSE_MATRIX_FAST_TRANSPOSE_HPP

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO1 | Practical 02
 * Topic: 2D Arrays & Sparse Matrix 3-Tuple Representation with O(cols + terms) Fast Transpose
 */
struct SparseTile {
    int row;      // Y grid tile coordinate
    int col;      // X grid tile coordinate
    int tileType; // 1: Stone Floor, 2: Cyber Pillar, 3: Hazard
};

class SparseArenaGrid {
private:
    static const int MAX_TILES = 120;
    SparseTile tiles[MAX_TILES];
    int numRows;
    int numCols;
    int numTiles;

public:
    SparseArenaGrid(int r = 18, int c = 32) : numRows(r), numCols(c), numTiles(0) {
        // Initialize arcade arena platform layout (Sparse 3-tuples)
        // Floor tiles across bottom
        for (int c_idx = 2; c_idx < 30; c_idx += 2) {
            addTile(1, c_idx, 1);
        }
        // Left & Right Pillars
        addTile(4, 4, 2);
        addTile(8, 4, 2);
        addTile(4, 27, 2);
        addTile(8, 27, 2);
        // Center Floating Battle Platform
        for (int c_idx = 12; c_idx <= 19; c_idx++) {
            addTile(6, c_idx, 1);
        }
    }

    void addTile(int r, int c, int type) {
        if (numTiles < MAX_TILES) {
            tiles[numTiles++] = {r, c, type};
        }
    }

    // Fast Transpose: O(cols + numTiles)
    // Uses column frequency counting and starting index mapping
    SparseArenaGrid fastTranspose() const {
        SparseArenaGrid result(numCols, numRows);
        result.numTiles = numTiles;
        if (numTiles <= 0) return result;

        int colFrequency[64] = {0};
        int startPosition[64] = {0};

        // Step 1: Count occurrences of each column
        for (int i = 0; i < numTiles; i++) {
            if (tiles[i].col < 64) colFrequency[tiles[i].col]++;
        }

        // Step 2: Calculate prefix sums for direct destination addresses
        startPosition[0] = 0;
        for (int i = 1; i < numCols && i < 64; i++) {
            startPosition[i] = startPosition[i - 1] + colFrequency[i - 1];
        }

        // Step 3: Directly place each element in transposed position
        for (int i = 0; i < numTiles; i++) {
            int targetCol = tiles[i].col;
            int destIdx = startPosition[targetCol];
            result.tiles[destIdx].row = tiles[i].col;
            result.tiles[destIdx].col = tiles[i].row;
            result.tiles[destIdx].tileType = tiles[i].tileType;
            startPosition[targetCol]++;
        }

        return result;
    }

    int getTileCount() const { return numTiles; }
    const SparseTile& getTile(int idx) const { return tiles[idx]; }
};

#endif // CO1_PRAC02_2D_SPARSE_MATRIX_FAST_TRANSPOSE_HPP
