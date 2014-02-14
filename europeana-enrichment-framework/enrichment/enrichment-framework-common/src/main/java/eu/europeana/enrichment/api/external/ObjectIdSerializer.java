package eu.europeana.enrichment.api.external;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;


public class ObjectIdSerializer extends SerializerBase<ObjectId> {


	public ObjectIdSerializer(){
		super(ObjectId.class);
	}
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void serialize(ObjectId value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		log.info("Entered in serialization \n");
		jgen.writeString(value.toString());

	}

}
