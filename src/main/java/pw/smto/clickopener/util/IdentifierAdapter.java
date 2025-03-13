package pw.smto.clickopener.util;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.Identifier;

public class IdentifierAdapter extends TypeAdapter<Identifier> {
	@Override
	public Identifier read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		return Identifier.of(in.nextString());
	}

	@Override
	public void write(JsonWriter out, Identifier id) throws IOException {
		out.value(id == null ? null : id.toString());
	}
}
