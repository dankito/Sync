package net.dankito.communication.message;


public class Response<T> {

  protected boolean couldHandleMessage;

  protected ResponseErrorType errorType;

  protected Exception error;

  protected T body;


  protected Response() { // for Jackson

  }

  public Response(ResponseErrorType errorType, Exception error) {
    this.couldHandleMessage = false;
    this.errorType = errorType;
    this.error = error;
  }

  public Response(T body) {
    this.couldHandleMessage = true;
    this.body = body;
  }


  public boolean isCouldHandleMessage() {
    return couldHandleMessage;
  }

  public ResponseErrorType getErrorType() {
    return errorType;
  }

  public Exception getError() {
    return error;
  }

  public T getBody() {
    return body;
  }


  @Override
  public String toString() {
    if(isCouldHandleMessage()) {
      return "Success: " + getBody();
    }
    else {
      return getErrorType() + ": " + getError();
    }
  }

}
