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
        UserDto userDto = mapper.map(user, UserDto.class);
        log.debug("converting " + user + " to dto: " + userDto);
        return userDto;
    }

    public User convertDtoToDocument(UserDto userDto) {
        User user = mapper.map(userDto, User.class);
        log.debug("converting " + userDto + " to document: " + user);
        return user;
    }

}
