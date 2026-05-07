package br.com.senai.model.converter;

import br.com.senai.model.enums.ServiceModality;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ServiceModalityConverter implements AttributeConverter<ServiceModality, String> {

    @Override
    public String convertToDatabaseColumn(ServiceModality attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ServiceModality convertToEntityAttribute(String dbData) {
        return ServiceModality.fromValue(dbData);
    }
}
