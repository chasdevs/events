protocol Events {

  @namespace("example.common")
  record ${name} {

    //a nullable string
    union { null, string } foo;
    //a non-nullable string
    string bar;

  }
}