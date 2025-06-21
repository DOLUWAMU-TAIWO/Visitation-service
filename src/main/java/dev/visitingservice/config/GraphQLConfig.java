package dev.visitingservice.config;

import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static graphql.scalars.ExtendedScalars.Date;
import static graphql.scalars.ExtendedScalars.DateTime;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(Date)
                .scalar(DateTime);
    }

    @Bean
    public GraphQLScalarType dateScalar() {
        return Date;
    }

    @Bean
    public GraphQLScalarType dateTimeScalar() {
        return DateTime;
    }
}
