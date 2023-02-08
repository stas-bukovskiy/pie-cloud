package com.piecloud.addition;


import com.piecloud.image.ImageNameToURLConverterGenerator;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdditionConverter {

    private final ModelMapper mapper;

    public AdditionConverter() {
        mapper = createModelMapper();
    }

    private ModelMapper createModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        modelMapper.createTypeMap(AdditionDto.class, Addition.class);
        modelMapper.createTypeMap(Addition.class, AdditionDto.class);
        modelMapper.getTypeMap(AdditionDto.class, Addition.class)
                .addMappings(mapper -> mapper.skip(Addition::setId));
        modelMapper.getTypeMap(Addition.class, AdditionDto.class)
                .addMappings(mapping -> mapping.using(ImageNameToURLConverterGenerator.generate())
                        .map(Addition::getImageName, AdditionDto::setImageUrl));
        return modelMapper;
    }

    public AdditionDto convertDocumentToDto(Addition addition){
        AdditionDto additionDto = mapper.map(addition, AdditionDto.class);
        log.debug("converting " + addition + " to dto: " + additionDto);
        return additionDto;
    }

    public Addition convertDtoToDocument(AdditionDto additionDto) {
        Addition addition = mapper.map(additionDto, Addition.class);
        log.debug("converting " + additionDto + " to document: " + addition);
        return addition;
    }

}
