package net.dankito.sync.communication.message;


public class Request<T> {

  protected String method;

  protected T body;


  public Request(String method) {
    this.method = method;
  }

  public Request(String method, T body) {
    this(method);
    this.body = body;
  }


  public String getMethod() {
    return method;
  }

  public boolean isBodySet() {
    return body != null;
  }

  public T getBody() {
    return body;
  }


  @Override
  public String toString() {
    return getMethod() + ": " + getBody();
  }

}
