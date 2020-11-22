package com.fitmap.function.studentcontext.v1.payload.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRequestDtos {

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Student {

        @Valid
        private List<@NotNull Contact> contacts;

        @Valid
        private List<@NotNull Address> addresses;

    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Contact {

        @Size(max = 500)
        private String name;

        @Email
        @Size(max = 500)
        private String email;

        @Size(max = 50)
        private String phone;

        @JsonProperty(value = "is_whatsapp")
        private Boolean whatsapp;
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Address {

        @Size(max = 50)
        private String zipCode;

        @Size(max = 300)
        private String publicPlace;

        @Size(max = 300)
        private String complement;

        @Size(max = 300)
        private String district;

        @Size(max = 300)
        private String city;

        @Size(max = 300)
        private String federalUnit;
    }
}
