package demo.application.properties.yaml.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.*;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
        PropertySource<?> ps = new PropertiesPropertySource("", new java.util.Properties());
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(encodedResource.getResource());

            Properties properties = factory.getObject();

            ps = new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
        }catch(Exception e){
            String fileName = encodedResource.getResource().getFilename();
            System.out.println("cannot load '${fileName}' : " + e);
        }

        return ps;
    }

}
