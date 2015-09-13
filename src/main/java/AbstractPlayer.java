//  Abstract Player - abstraction with which Referee plays, to separate AI impl
public abstract class AbstractPlayer {
  public abstract void receiveState();
  public abstract Move getMove();
  public abstract void newMatch();
}