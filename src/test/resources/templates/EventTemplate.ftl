protocol Events {

  import idl "${relativePathToRepoRoot}/common/Meta.avdl";

  @namespace("${namespace}")
  record ${name} {
    example.common.Meta meta;

    //a nullable string
    union { null, string } foo;
    //a non-nullable string
    string bar;

  }
}