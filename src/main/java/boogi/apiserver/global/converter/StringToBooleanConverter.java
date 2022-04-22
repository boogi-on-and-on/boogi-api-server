package boogi.apiserver.global.converter;

import org.springframework.core.convert.converter.Converter;

public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public Boolean convert(String source) {
        source = source.toLowerCase();
        switch (source) {
            case "true":
                return Boolean.TRUE;
            default:
                return Boolean.FALSE;
        }
    }
}
