package net.dankito.communication.util;


public class TestResponseBody {

  protected String string;

  protected float aFloat;


  public TestResponseBody() {

  }

  public TestResponseBody(String string, float aFloat) {
    this.string = string;
    this.aFloat = aFloat;
  }


  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public float getaFloat() {
    return aFloat;
  }

  public void setaFloat(float aFloat) {
    this.aFloat = aFloat;
  }

}
