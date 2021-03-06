import java.io.*;
import java.io.File;
import java.nio.file.Files;
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
  boolean isExploring;
  int moveNumber;
  double winScore;
  double loseScore;
  double drawScore;
  boolean verbose;

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
    verbose = false;
    isLearning = true;
    isExploring = true;
    winScore = 1.;
    loseScore = -500.0;
    drawScore = 0;
    learningRate = 0.7;
    discountFactor = 0.9;
    this.playerNumber=playerNumber;
  }

  public void setVerbosity(boolean verbosity) {
    verbose = verbosity;
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
      //if move is illegal and same state is returned
      if (findIndex(state) == previousIndex){	
	  qValues[previousIndex][previousMove] = -10000;
	  //System.out.println("qValue: " + qValues[previousIndex][previousMove]);
	 }
      else
	 {
	    qValues[previousIndex][previousMove] = qValues[previousIndex][previousMove]
      + learningRate * ( reward(previousIndex) + discountFactor * 
        maxQDiff(state, previousIndex, previousMove));
	 }
      //System.out.println("currentState index: " + findIndex(state));
      //System.out.println("previousState index: " + previousIndex);
  }

  private double reward(int previousIndex) {
    return 0.;
  }


    //finds maximum distance between qValue(current state) and qValue[previous_state][i] where i is the 9 possible moves
  public double maxQDiff(TicTacToeState state, int previousIndex,
                         int previousMove) {
    double best = qValues[findIndex(state)][0] -
                  qValues[previousIndex][previousMove];
    for (int i = 1; i < 9; i++) {
      if ((qValues[findIndex(state)][i] - 
        qValues[previousIndex][previousMove]) > best) {
        best = (qValues[findIndex(state)][i] - 
                qValues[previousIndex][previousMove]);
      }
    }
    return best;
  }

  public void saveBrain(String brainName){
      try {
	  File fileQVal = new File(brainName + "QVal.csv");
	  FileWriter fwQVal = new FileWriter(fileQVal.getAbsoluteFile());
	  BufferedWriter bwQVal = new BufferedWriter(fwQVal);
	  for (int i = 0; i < size; i++){
	    for (int j = 0; j < 8; j++){
	        bwQVal.write(qValues[i][j]+",");
	    }
	    bwQVal.write(qValues[i][8]+"\n");
	  }
	  bwQVal.close();

	  File fileQNum = new File(brainName + "QNum.csv");
	  FileWriter fwQNum = new FileWriter(fileQNum.getAbsoluteFile());
	  BufferedWriter bwQNum = new BufferedWriter(fwQNum);
	  for (int i = 0; i < size; i++){
	      for (int j = 0; j < 8; j++){
		  bwQNum.write(qUpdateNum[i][j]+",");
	      }
	      bwQNum.write(qUpdateNum[i][8]+"\n");
	  }
	  bwQNum.close();
      } catch (IOException e){
	  e.printStackTrace();
      }    
  }  
    public void loadBrain(String brainName) throws FileNotFoundException{
	try
	{
	    Scanner scannerQVal = new Scanner(new File(brainName + "QVal.csv"));
	    String delimiter = ",";
	    for (int i = 0; i < size; i++) {
            String line = scannerQVal.nextLine();
            String[] splitLine = line.split(delimiter);
            for (int j = 0; j < 9; j++) {
                qValues[i][j] = Double.parseDouble(splitLine[j]);
            }
        }

        scannerQVal.close();
	    Scanner scannerQNum = new Scanner(new File(brainName + "QNum.csv"));
	    scannerQNum.useDelimiter(",");
	    for (int i = 0; i < size; i++) {
            String line = scannerQNum.nextLine();
            String[] splitLine = line.split(delimiter);
            for (int j = 0; j < 9; j++) {
                qUpdateNum[i][j] = Integer.parseInt(splitLine[j]);
            }
	    }
	    scannerQNum.close();
	}
	catch(FileNotFoundException fnfe)
	{
	    throw new FileNotFoundException(("File not found"));
	}
    }

    public void clearBrain(String brainName){
        File f = new File(brainName + "QVal.csv");
        f.delete();
        File g = new File(brainName + "QNum.csv");
        g.delete();
        System.out.println("Brain " + brainName + " cleared.");
    }

  @Override
  public TicTacToeMove getMove(){
      return nextMove;
  }

  private double exploreScore(int leastExplored) {
    if (isExploring == true) {
      return 1./((leastExplored)+3);
    }
    else 
      return -1;
  }

  public void setExploring(boolean exploring) {
    isExploring = exploring;
  }

  private void bestMove(int index) {
    double bestQval=qValues[index][0]; // sets initial value
    int leastExplored = qUpdateNum[index][0]; // number of times rare move was explored
    int expMoveIndex=0;
    int qMoveIndex=0;
    for (int i = 1; i < 9; i++) {
      if (qUpdateNum[index][i] < leastExplored) {
        leastExplored = qUpdateNum[index][i];
        expMoveIndex = i;
      }
      //System.out.println(i + ": " + qValues[index][i]);
      if (qValues[index][i] > bestQval) {
        bestQval = qValues[index][i];
        qMoveIndex = i;
      }
      
    }
    //System.out.println("best qValue,qIndex: " + bestQval + "," + qMoveIndex);
    TicTacToeMove thisMove;
    Random rand = new Random();
    if (rand.nextDouble() < exploreScore(leastExplored)) {
      previousMove=expMoveIndex;
      qUpdateNum[index][expMoveIndex]++;
      thisMove = new TicTacToeMove(this.playerNumber,expMoveIndex/3,expMoveIndex%3);
    }
    else {
      previousMove=qMoveIndex;
      //System.out.println("winner is ");
      qUpdateNum[index][qMoveIndex]++;
      thisMove = new TicTacToeMove(this.playerNumber,qMoveIndex/3,qMoveIndex%3);
    }
    moveNumber++;
    nextMove = thisMove;
    previousIndex = index;
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
      previousIndex = findIndex(ticState);
      bestMove(findIndex(ticState));
    }
  }

  @Override
  public void receiveResult(int result) {
    if (result == playerNumber) {
	qValues[previousIndex][previousMove] = winScore; //set winScore to avoid overflow
    }
    else if (result == -1) {
      qValues[previousIndex][previousMove] = drawScore;
    }
    else {
      qValues[previousIndex][previousMove] = loseScore;
    }
  }
}
