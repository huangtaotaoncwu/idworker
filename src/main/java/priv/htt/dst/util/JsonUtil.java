package priv.htt.dst.util;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;

public class JsonUtil {

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.ALWAYS);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.setSerializationConfig(mapper.getSerializationConfig()
                .without(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS));
        mapper.setDeserializationConfig(mapper.getDeserializationConfig()
                .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES));
        return mapper;
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            if (StringUtils.isNotBlank(jsonString)) {
                return buildMapper().readValue(jsonString, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> valueTypeRef) {
        try {
            if (StringUtils.isNotBlank(jsonString)) {
                return buildMapper().readValue(jsonString, valueTypeRef);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Object object) {
        try {
            return buildMapper().writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
