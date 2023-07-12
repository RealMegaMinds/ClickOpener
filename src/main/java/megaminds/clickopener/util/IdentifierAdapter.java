package megaminds.clickopener.util;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.Identifier;

public class IdentifierAdapter extends TypeAdapter<Identifier> {
	@Override
	public Identifier read(JsonReader arg0) throws IOException {
		return new Identifier(TypeAdapters.STRING.read(arg0));
	}

	@Override
	public void write(JsonWriter arg0, Identifier arg1) throws IOException {
		TypeAdapters.STRING.write(arg0, arg1.toString());
	}
}
