package HelloWorldAPI; // to be updated with binding information

import io.mdsl.annotations.ServiceEndpoint;
import io.mdsl.annotations.ServiceOperation;


/* 
// TODO warn if there is more than one Java binding, use correct one
// TODO use Java class name from location in transformation (or map to @Bean name for container?)
Endpoint provider locations and operation bindings:

*/




// ** endpoints:

@ServiceEndpoint() // TODO otherRoles not used yet
public interface HelloWorldEndpoint {
    // ** operations:
    @ServiceOperation() // TODO responsibilities (see endpoint roles)
    SampleDTO sayHello(String in); 
    
    @ServiceOperation() // TODO responsibilities (see endpoint roles)
    SampleDTO sayHelloMultipleTimes(String[] in); 
    
    @ServiceOperation() // TODO responsibilities (see endpoint roles)
    NestedDTO sayHelloNested(SampleDTO in); 
    
    @ServiceOperation() // TODO responsibilities (see endpoint roles)
    java.lang.String sayHelloGenericParameters(java.lang.String p12); 
    

  // ** inner types: 

  class AnonymousTree1Class { int key; boolean[] value; }
  class SampleDTO { AnonymousTree1Class anonymousTree1Instance;}
  class AnonymousTree2Class { int counter; java.lang.String[] links; SampleDTO values; GenericParameters p0; AllBaseTypes baseTypeTest; ParameterForest pfTest; }
  class NestedDTO { AnonymousTree2Class anonymousTree2Instance;}
  class AnonymousTree3Class { String p1;  String tbd;  java.lang.String p2; java.lang.String tbd2; byte p3; byte raw; }
  class GenericParameters { AnonymousTree3Class anonymousTree3Instance;}
  class AnonymousTree4Class { int p4; long p5; double p6; java.lang.String p7; byte p8; java.lang.Object aVoid; }
  class AllBaseTypes { AnonymousTree4Class anonymousTree4Instance;}
  class ParameterForest { int notYet;;}

}

