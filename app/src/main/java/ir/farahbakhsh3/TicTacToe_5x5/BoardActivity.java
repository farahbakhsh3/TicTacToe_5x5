package ir.farahbakhsh3.TicTacToe_5x5;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class BoardActivity extends AppCompatActivity {

    class Move {
        int row;
        int col;
        int val;
    }

    Random rand = new Random();

    public static final int ONE_PLAYER = 0;
    public static final int TWO_PLAYER = 1;
    public static final int GAME_LEVEL_BEGINNER = 50;
    public static final int GAME_LEVEL_EASY = 60;
    public static final int GAME_LEVEL_NORMAL = 85;
    public static final int GAME_LEVEL_HARD = 100;
    final int iDEPTH1 = 5;
    final int iDEPTH2 = 5;
    final int iDEPTH3 = 7;
    final int iDEPTH4 = 7;
    final int iDEPTHMAX = 99;
    final int RED_CODE_HUMAN = 1;
    final int RED_CODE_HUMAN_DB_SCORE_idx = 0;
    final int YELLOW_CODE_AI = 2;
    final int YELLOW_CODE_AI_DB_SCORE_idx = 2;
    final int NOT_PLAYED = 0;
    final int DRAW = 0;
    final int DRAW_DB_SCORE_idx = 1;
    final int NO_WINNER_DRAW = -1;
    final int BOARD_SIZE = 5;
    final int GOAL_SIZE = 4;
    final int SCORE_MAX = 1000;
    final int SCORE_MIN = -1000;
    final int SCORE_PLUS = 50;
    final int SCORE_MINUS = -50;
    final int SCORE_ZERO = DRAW;
    int game_level;
    int[][] gameBoard = new int[BOARD_SIZE][BOARD_SIZE];
    int play_type = ONE_PLAYER;
    int activePlayer = RED_CODE_HUMAN;
    int score = NO_WINNER_DRAW;
    boolean AI_turn = false;
    int DEPTH = -1;
    int idxDepth;

    RelativeLayout msgLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        msgLayout = findViewById(R.id.msg_layout);
        msgLayout.setVisibility(View.GONE);
        Bundle extras = getIntent().getExtras();
        play_type = extras.getInt(getString(R.string.play_type));
        game_level = extras.getInt(getString(R.string.game_level));
        reset();
    }

    public void dropIn(View view) {

        int tag = Integer.parseInt((String) view.getTag());
        int row = tag / BOARD_SIZE;
        int col = tag % BOARD_SIZE;

        if (gameBoard[row][col] != NOT_PLAYED)
            return;
        if (evaluateBoard(gameBoard, GOAL_SIZE, false) != SCORE_ZERO)
            return;
        if (play_type == ONE_PLAYER)
            if (activePlayer == YELLOW_CODE_AI)
                if (!AI_turn)
                    return;

        ImageView img = (ImageView) view;
        img.setTranslationY(-2000f);
        if (activePlayer == YELLOW_CODE_AI) {
            if (play_type == ONE_PLAYER)
                img.setImageResource((R.drawable.yellow_ai));
            else if (play_type == TWO_PLAYER)
                img.setImageResource(R.drawable.yellow);
            gameBoard[row][col] = activePlayer;
            activePlayer = RED_CODE_HUMAN;
            img.animate().translationY(0f).setDuration(150);
        } else if (activePlayer == RED_CODE_HUMAN) {
            if (play_type == ONE_PLAYER)
                img.setImageResource(R.drawable.red_human);
            if (play_type == TWO_PLAYER)
                img.setImageResource(R.drawable.red);
            gameBoard[row][col] = activePlayer;
            activePlayer = YELLOW_CODE_AI;
            img.animate().translationY(0f).setDuration(150);
        }

        if (winnerMsg(gameBoard) == NO_WINNER_DRAW) {
            if ((play_type == ONE_PLAYER) && (activePlayer == YELLOW_CODE_AI)) {
                findBestMove(gameBoard);
            }
        }
    }

    public int winnerMsg(int[][] board) {
        score = evaluateBoard(board, GOAL_SIZE, false);
        if (!isMovesLeft(board) || (score != SCORE_ZERO)) {
            int levelIdx = 0;
            switch (game_level) {
                case GAME_LEVEL_BEGINNER:
                    levelIdx = 0;
                    break;
                case GAME_LEVEL_EASY:
                    levelIdx = 1;
                    break;
                case GAME_LEVEL_NORMAL:
                    levelIdx = 2;
                    break;
                case GAME_LEVEL_HARD:
                    levelIdx = 3;
                    break;
            }

            SQLiteHelper sql = new SQLiteHelper(getApplicationContext());
            String msg = "";
            int color = Color.GREEN;
            if (score == SCORE_ZERO) {
                msg = getString(R.string.draw);
                if (play_type == ONE_PLAYER)
                    sql.setScore(levelIdx, DRAW_DB_SCORE_idx);
            } else if (score == SCORE_MINUS) {
                if (play_type == ONE_PLAYER) {
                    msg = getString(R.string.human);
                    sql.setScore(levelIdx, RED_CODE_HUMAN_DB_SCORE_idx);
                } else if (play_type == TWO_PLAYER) {
                    msg = getString(R.string.red);
                }
                color = Color.RED;
            } else if (score == SCORE_PLUS) {
                if (play_type == ONE_PLAYER) {
                    msg = getString(R.string.AI);
                    sql.setScore(levelIdx, YELLOW_CODE_AI_DB_SCORE_idx);
                } else if (play_type == TWO_PLAYER) {
                    msg = getString(R.string.yellow);
                }
                color = Color.YELLOW;
            }
            msgLayout.setBackgroundColor(color);
            msg = getString(R.string.winner) + msg;
            ((TextView) msgLayout.findViewById(R.id.winner_message)).setText(msg);
            msgLayout.setVisibility(View.VISIBLE);

            return score;
        }
        return NO_WINNER_DRAW;
    }

    public void reset() {
        score = NO_WINNER_DRAW;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                gameBoard[row][col] = NOT_PLAYED;
            }
        }

        LinearLayout pgLayout = findViewById(R.id.pg_layout);
        for (int i = 0; i < pgLayout.getChildCount(); i++) {
            LinearLayout row = (pgLayout.getChildAt(i) instanceof LinearLayout) ?
                    (LinearLayout) pgLayout.getChildAt(i) : null;
            if (row != null) {
                for (int j = 0; j < row.getChildCount(); j++) {
                    ImageView iv = (row.getChildAt(j) instanceof ImageView) ?
                            (ImageView) row.getChildAt(j) : null;
                    if (iv != null) {
                        iv.setImageResource(0);
                    }
                }
            }
        }

        msgLayout.setVisibility(View.GONE);

        activePlayer = rand.nextBoolean() ? RED_CODE_HUMAN : YELLOW_CODE_AI;
        String message = "";
        if (play_type == TWO_PLAYER) {
            if (activePlayer == RED_CODE_HUMAN)
                message = getString(R.string.red);
            else
                message = getString(R.string.yellow);
        } else if (play_type == ONE_PLAYER) {
            if (activePlayer == RED_CODE_HUMAN)
                message = getString(R.string.human);
            else
                message = getString(R.string.AI);
            if (activePlayer == YELLOW_CODE_AI)
                findBestMove(gameBoard);
        }
        message = getString(R.string.play_start) + message;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem resetItem = menu.add(R.string.play_again);
        resetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        resetItem.setOnMenuItemClickListener(this::onResetMenuClick);
        return true;
    }

    public boolean onResetMenuClick(MenuItem menuItem) {
        reset();
        return true;
    }

    public void onResetButtonClick(View view) {
        reset();
    }

    public int NOT_PLAYED_counter(int[][] board) {
        int cnt = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == NOT_PLAYED) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public Boolean isMovesLeft(int[][] board) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == NOT_PLAYED) {
                    return true;
                }
            }
        }
        return false;
    }

    public int evaluateBoard(int[][] board, int GOAL_SIZE, boolean recursive) {
        if (recursive) {
            if (GOAL_SIZE < 3) {
                return SCORE_ZERO;
            }
        }

        boolean human = true;
        boolean AI = true;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE - GOAL_SIZE + 1; col++) {
                human = true;
                AI = true;
                for (int chk = 0; chk < GOAL_SIZE; chk++) {
                    if (board[row][col + chk] != RED_CODE_HUMAN)
                        human = false;
                    if (board[row][col + chk] != YELLOW_CODE_AI)
                        AI = false;
                }
                if (human)
                    return SCORE_MINUS;
                if (AI)
                    return SCORE_PLUS;
            }
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            for (int row = 0; row < BOARD_SIZE - GOAL_SIZE + 1; row++) {
                human = true;
                AI = true;
                for (int chk = 0; chk < GOAL_SIZE; chk++) {
                    if (board[row + chk][col] != RED_CODE_HUMAN)
                        human = false;
                    if (board[row + chk][col] != YELLOW_CODE_AI)
                        AI = false;
                }
                if (human)
                    return SCORE_MINUS;
                if (AI)
                    return SCORE_PLUS;
            }
        }

        for (int row = 0; row < BOARD_SIZE - GOAL_SIZE + 1; row++) {
            for (int col = 0; col < BOARD_SIZE - GOAL_SIZE + 1; col++) {
                human = true;
                AI = true;
                for (int chk = 0; chk < GOAL_SIZE; chk++) {
                    if (board[row + chk][col + chk] != RED_CODE_HUMAN)
                        human = false;
                    if (board[row + chk][col + chk] != YELLOW_CODE_AI)
                        AI = false;
                }
                if (human)
                    return SCORE_MINUS;
                if (AI)
                    return SCORE_PLUS;
            }
        }

        for (int row = 0; row < BOARD_SIZE - GOAL_SIZE + 1; row++) {
            for (int col = GOAL_SIZE - 1; col < BOARD_SIZE; col++) {
                human = true;
                AI = true;
                for (int chk = 0; chk < GOAL_SIZE; chk++) {
                    if (board[row + chk][col - chk] != RED_CODE_HUMAN)
                        human = false;
                    if (board[row + chk][col - chk] != YELLOW_CODE_AI)
                        AI = false;
                }
                if (human)
                    return SCORE_MINUS;
                if (AI)
                    return SCORE_PLUS;
            }
        }

        if (recursive)
            return evaluateBoard(board, GOAL_SIZE - 1, true) / 2;
        return SCORE_ZERO;
    }

    public int minimax(int[][] board, int depth, Boolean isMax, int alpha, int beta) {

        int score = evaluateBoard(board, GOAL_SIZE, true);
        if (score == SCORE_PLUS)
            return score - depth;
        if (score == SCORE_MINUS)
            return score + depth;
        if (!isMovesLeft(board))
            return score;
        if (depth > DEPTH)
            return score;

        idxDepth = Math.max(idxDepth, depth);
        if (isMax) {
            int best = SCORE_MIN;
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == NOT_PLAYED) {
                        board[row][col] = YELLOW_CODE_AI;

                        int val = minimax(board, depth + 1, false, alpha, beta);
                        best = Math.max(best, val);
                        alpha = Math.max(alpha, best);
                        board[row][col] = NOT_PLAYED;

                        if (beta <= alpha)
                            break;
                    }
                }
            }
            return best;
        } else {
            int best = SCORE_MAX;
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == NOT_PLAYED) {
                        board[row][col] = RED_CODE_HUMAN;

                        int val = minimax(board, depth + 1, true, alpha, beta);
                        best = Math.min(best, val);
                        beta = Math.min(beta, best);
                        board[row][col] = NOT_PLAYED;

                        if (beta <= alpha)
                            break;
                    }
                }
            }
            return best;
        }
    }

    public int CalcDepth(int[][] board) {
        int counter = NOT_PLAYED_counter(board);
        if (counter >= 20) return iDEPTH1;
        else if (counter >= 17) return iDEPTH2;
        else if (counter >= 14) return iDEPTH3;
        else if (counter >= 11) return iDEPTH4;
        else return iDEPTHMAX;
    }

    public boolean isNeighborN(int[][] board, int row, int col, int N_NEIGHBOR) {
        for (int i = -N_NEIGHBOR; i <= N_NEIGHBOR; i++) {
            if (row + i >= 0 && row + i < BOARD_SIZE) {
                for (int j = -N_NEIGHBOR; j <= N_NEIGHBOR; j++) {
                    if (col + j >= 0 && col + j < BOARD_SIZE) {
                        if (i == 0 || j == 0 || Math.abs(i) == Math.abs(j)) {
                            if (board[row + i][col + j] != NOT_PLAYED) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void findBestMove(int[][] board) {
        Move bestMove = new Move();
        bestMove.row = -1;
        bestMove.col = -1;
        bestMove.val = SCORE_MIN;

        ArrayList<int[]> lst = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                DEPTH = CalcDepth(board);
                idxDepth = 0;
                if (board[row][col] == NOT_PLAYED) {
                    board[row][col] = YELLOW_CODE_AI;
                    int moveVal = minimax(board, 0, false, SCORE_MIN, SCORE_MAX);
                    board[row][col] = NOT_PLAYED;

                    if (moveVal > bestMove.val) {
                        bestMove.row = row;
                        bestMove.col = col;
                        bestMove.val = moveVal;
                        int[] x = {bestMove.row, bestMove.col, bestMove.val};
                        lst.clear();
                        lst.add(x);
                    } else if (moveVal == bestMove.val) {
                        bestMove.row = row;
                        bestMove.col = col;
                        bestMove.val = moveVal;
                        int[] x = {bestMove.row, bestMove.col, bestMove.val};
                        lst.add(x);
                    }
                    String m = row + "," + col + ":" + (row * BOARD_SIZE + col) + "\t"
                            + "\t depth: " + idxDepth + "\t Score: " + moveVal;
                    Log.i("tag: ", m);
                }
            }
        }

        int rnd = rand.nextInt(100);
        if (rnd > game_level) {
            Do_Random_Move(gameBoard);
            return;
        }

        if (lst.size() > 0) {
            int r = rand.nextInt(lst.size());
            bestMove.row = lst.get(r)[0];
            bestMove.col = lst.get(r)[1];
            bestMove.val = lst.get(r)[2];

            String m = "ROW: " + bestMove.row + " COL: " + bestMove.col;
            Log.i("tag", m);

            doIT(bestMove);
        } else {
            Do_First_Move();
        }
    }

    public void doIT(Move move) {
        int tag = move.row * BOARD_SIZE + move.col;
        LinearLayout pgLayout = findViewById(R.id.pg_layout);
        for (int i = 0; i < pgLayout.getChildCount(); i++) {
            LinearLayout row = (pgLayout.getChildAt(i) instanceof LinearLayout) ?
                    (LinearLayout) pgLayout.getChildAt(i) : null;
            if (row != null) {
                for (int j = 0; j < row.getChildCount(); j++) {
                    ImageView iv = (row.getChildAt(j) instanceof ImageView) ?
                            (ImageView) row.getChildAt(j) : null;
                    if (iv != null) {
                        if (Integer.parseInt(iv.getTag().toString()) == tag) {
                            AI_turn = true;
                            dropIn(iv);
                            AI_turn = false;
                        }
                    }
                }
            }
        }
    }

    public void Do_Random_Move(int[][] board) {
        ArrayList<int[]> lst = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == NOT_PLAYED) {
                    int[] x = {row, col, SCORE_ZERO};
                    lst.add(x);
                }
            }
        }
        int r = rand.nextInt(lst.size());
        Move randomMove = new Move();
        randomMove.row = lst.get(r)[0];
        randomMove.col = lst.get(r)[1];
        randomMove.val = lst.get(r)[2];

        doIT(randomMove);
    }

    public void Do_First_Move() {
        int rndRowIdx = rand.nextInt(3) - 1;
        int rndColIdx = rand.nextInt(3) - 1;

        Move move = new Move();
        move.row = BOARD_SIZE / 2 + rndRowIdx;
        move.col = BOARD_SIZE / 2 + rndColIdx;
        move.val = SCORE_ZERO;

        doIT(move);
    }
}
