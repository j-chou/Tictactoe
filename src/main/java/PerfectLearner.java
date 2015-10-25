import java.io.*;
import java.util.*;

public class PerfectLearner extends AbstractPlayer
{
  TicTacToeMove nextMove;
  double[][] qValues;
  int[][] qUpdateNum;
  double learningRate;
  double discountFactor;
  int previousIndex;
  int previousMove;
  boolean isLearning;
  int moveNumber;
  double winScore;
  double loseScore;
  double drawScore;

  int size = (int) Math.pow(3,9);
    
  public PerfectLearner(int playerNumber) {
    qUpdateNum = new int[size][9];
    qValues = new double[size][9];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 9; j++) {
        qValues[i][j] = 0.;
        qUpdateNum[i][j] = 0;
      }
    }
    isLearning = true;
    winScore=10;
    loseScore=-10;
    drawScore=0;
  }

  //  set learning parameteres
  public void setParameters(double learning_rate, double discount_factor) {
    learningRate = learning_rate;
    discountFactor = discount_factor;
  }

  //  returns an index of the state in the qValue array
  public int findIndex(TicTacToeState state) {
    int index = 0;
    for (int i = 0; i < state.sizeX; i++) {
      for (int j = 0; j < state.sizeY; j++) {
        index = 3 * index + state.board[i][j];
      }
    }
    return index;
  }

  public void updateQ(TicTacToeState state) {
    qValues[previousIndex][previousMove] = qValues[previousIndex][previousMove]
      + learningRate * ( reward(previousIndex) + discountFactor * 
        maxQDiff(state, previousIndex, previousMove));
  }

  private double reward(int previousIndex) {
    return -0.01;
  }

  public double maxQDiff(TicTacToeState state, int previousIndex,
    int previousMove) {
    double best = qValues[findIndex(state)][0] -
      qValues[previousIndex][previousMove];
    for (int i = 0; i < 9; i++) {
      if ((qValues[findIndex(state)][i] - 
        qValues[previousIndex][previousMove]) > best) {
        best = (qValues[findIndex(state)][i] - 
               qValues[previousIndex][previousMove]);
      }
    }
    return best;
  }

  public void saveBrain(){
      try {
	  File file = new File("brain.csv");
	  FileWriter fw = new FileWriter(file.getAbsoluteFile());
	  BufferedWriter bw = new BufferedWriter(fw);
	  for (int i = 0; i < size; i++){
	    bw.write(i);
	    for (int j = 0; j < 9; j++){
		    bw.write("," + qValues[i][j]);
	    }
	    bw.write("\n");
	  }
      } catch (IOException e){
	  e.printStackTrace();
      }
  }
  
    public void loadBrain(){}

  @Override
  public Move getMove(){
      return nextMove;
  }

  private double exploreScore(int leastExplored) {
    return 1./(leastExplored+1);
  }

  private void bestMove(int index) {
    double bestQval=qValues[index][0];
    int leastExplored = 0;
    int expMoveIndex=0;
    int qMoveIndex=0;
    for (int i = 0; i < 9; i++) {
      if (qUpdateNum[index][i] < leastExplored) {
        leastExplored = qUpdateNum[index][i];
        expMoveIndex = i;
      }
      if (qValues[index][i] > bestQval) {
        bestQval = qValues[index][i];
        qMoveIndex = i;
      }
    }
    TicTacToeMove thisMove;
    Random rand = new Random();
    if (rand.nextDouble() < exploreScore(leastExplored)) {
      thisMove = new TicTacToeMove(this.playerNumber,expMoveIndex/3,expMoveIndex%3);
    }
    else {
      thisMove = new TicTacToeMove(this.playerNumber,qMoveIndex/3,qMoveIndex%3);
    }
    nextMove = thisMove;
  }

  @Override
  public void newMatch(){}

  @Override
  public void receiveState(GameState state) {
    if(state instanceof TicTacToeState)
    {
      TicTacToeState ticState = (TicTacToeState)state;
    
      if ((moveNumber > 1) && isLearning) {
        updateQ(ticState);
      }
    bestMove(findIndex(ticState));
    }
  }

  @Override
  public void receiveResult(int result) {
    if (result == playerNumber) {
      qValues[previousIndex][previousMove] = (qValues[previousIndex][previousMove] +
       winScore);
    }
    if (result == -1) {
      qValues[previousIndex][previousMove] = (qValues[previousIndex][previousMove] +
        drawScore);
    }
    else {
      qValues[previousIndex][previousMove] = (qValues[previousIndex][previousMove] +
        loseScore);
    }
  }
}
