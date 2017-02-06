package net.dankito.sync.javafx.controls;


/**
 * As Spring Boot first creates a Bean = calls a constructor and then injects all dependencies,
 * dependencies cannot be used directly in constructor -> implement this interface, when init()
 * is called, dependencies are for sure injected.
 */
public interface Initializable {

  void init();

}
