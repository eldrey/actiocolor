/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Rest.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Eldrey
 */
@Path("/Deuteranopia")
public class Deuteranopia {

    /**
     *
     * @param jsonRequest
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    //@Consumes(MediaType.WILDCARD)
    public Response sayHello(String jsonRequest) throws ParseException {

        JSONObject jO = (JSONObject) new JSONParser().parse(jsonRequest);
        String tipo;
        if (jO.get("tipo") == null) {
            tipo = "jpg";
        } else {
            tipo = (jO.get("tipo").toString()).split("\"")[1];
        }
        String img = jO.get("imagemBase64").toString();
        String name = jO.get("nome").toString().split("\\.")[0];
        name = name.replace("?", "");
        name = name.replace(":", "");
        name = name.replace("-", "");
        //System.out.println(tipo + " - " + name + " - " + img);
        //name +"."+tipo = nome da imagem
        //img = Imagem em base64
        byte[] b;
        try {
            b = Base64.decode(img);
            FileOutputStream fo;
            fo = new FileOutputStream("D:\\" + name + "." + tipo);
            fo.write(b);
            Runtime run = Runtime.getRuntime();
            Process proc;

            proc = run.exec("java -jar D:\\UTFPR\\TCC\\prog\\ActioColorProcessamento\\dist\\ActioColorProcessamento.jar D:\\" + name + "." + tipo + " T");
            proc.waitFor();

            try (FileInputStream is = new FileInputStream("D:\\" + name + "." + tipo)) {
                b = new byte[is.available()];
                is.read(b);
            }
            File f = new File("D:\\" + name + "." + tipo);
            f.delete();
            //System.out.println("========================================================\n\n ");
            return Response.ok().entity("{"
                    + "\"nome\":\"" + name + "." + tipo + "\","
                    + "\"imagemBase64\": \"" + (new String(Base64.encodeBytesToBytes(b))) + "\""
                    + "}").header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methodos", "GET, POST").allow("OPTIONS").build();
        } catch (IOException ex) {
            Logger.getLogger(Deuteranopia.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Deuteranopia.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().encoding("ERROR").header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methodos", "GET, POST").allow("OPTIONS").build();
    }

}
