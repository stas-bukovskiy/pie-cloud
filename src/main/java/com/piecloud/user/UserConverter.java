package com.piecloud.user;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserConverter {

    private final ModelMapper mapper;

    public UserConverter() {
        mapper = createModelMapper();
    }

    private ModelMapper createModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        return modelMapper;
    }

    public UserDto convertDocumentToDto(User user){
        return mapper.map(user, UserDto.class);
    }

    public User convertDtoToDocument(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }

}
