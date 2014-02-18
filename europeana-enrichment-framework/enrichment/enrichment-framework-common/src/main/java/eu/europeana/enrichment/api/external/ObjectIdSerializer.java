package eu.europeana.enrichment.api.external;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * ObjectId mapper class for correct serialization of org.bson.types.ObjectId to
 * String
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
public class ObjectIdSerializer extends SerializerBase<ObjectId> {

	public ObjectIdSerializer() {
		super(ObjectId.class);
	}

	@Override
	public void serialize(ObjectId value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeString(value.toString());

	}

}
