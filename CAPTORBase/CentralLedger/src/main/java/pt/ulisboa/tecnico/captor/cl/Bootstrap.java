package pt.ulisboa.tecnico.captor.cl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "Swagger Server", 
        version = "2.0.1", 
        description = "These are the endpoints provided by the Central Ledger server of CAPTOR. These endpoints are used by the Inspect and Transport applications. The Inspect application will use the endpoints with inspection tags and the Transport application will use the endpoints with trip tags",
        termsOfService = "",
        contact = @Contact(email = ""),
        license = @License(
            name = "",
            url = "http://unlicense.org"
        )
    )
)
public class Bootstrap {
}
