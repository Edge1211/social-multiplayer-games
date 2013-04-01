package org.zhihanli.hw3;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
  @Source("black_tile.png")
  ImageResource blackTile();
  
  @Source("white_tile.png")
  ImageResource whiteTile();
  
  @Source("Black B.png")
  ImageResource blackBishop();
  
  @Source("Black K.png")
  ImageResource blackKing();
  
  @Source("Black N.png")
  ImageResource blackKnight();
  
  @Source("Black P.png")
  ImageResource blackPawn();
  
  @Source("Black Q.png")
  ImageResource blackQueen();
  
  @Source("Black R.png")
  ImageResource blackRook();
  
  @Source("White B.png")
  ImageResource whiteBishop();
  
  @Source("White K.png")
  ImageResource whiteKing();
  
  @Source("White N.png")
  ImageResource whiteKnight();
  
  @Source("White P.png")
  ImageResource whitePawn();
  
  @Source("White Q.png")
  ImageResource whiteQueen();
  
  @Source("White R.png")
  ImageResource whiteRook();
  
  @Source("empty.png")
  ImageResource empty();
  
  
  
}
