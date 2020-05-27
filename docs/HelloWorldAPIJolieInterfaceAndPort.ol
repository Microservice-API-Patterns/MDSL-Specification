// API title: HelloWorldAPI
// API description: The MDSL service contract specified the usage scenario as: UNDISCLOSED_VISIBILITY ANY_INTEGRATION_DIRECTION, serving in undisclosed MAP role(s)
// The WSDL can be viewed/analyzed at: https://www.wsdl-analyzer.com/upload



type helloWorldRequestDTO {
  
identifier1:string
  
}
type helloWorldResponseDTO {
  
identifier2:string
identifier3:string

  
}



type SOAPFaultMessage {
    code: int
    text: string
    actor: string
    details: string
}


// TODO only capitalize first character (last one is decapitalized?)
// interface/endpoint role: undisclosed
interface Helloworldendpoint {
// TODO also support OneWay:
RequestResponse:

    // operation responsibility: unknown
    helloWorld( helloWorldRequestDTO )( helloWorldResponseDTO )  , // TODO no comma for last op
}



inputPort HelloworldendpointPort {
location: "socket://localhost:8080" // this would come from API Provider Info in MDSL
protocol: soap
interfaces: Helloworldendpoint
}


main
{
  nullProcess
}