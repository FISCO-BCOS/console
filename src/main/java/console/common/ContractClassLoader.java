package console.common;

import java.io.File;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ContractClassLoader.class);

    private String classPath;

    public ContractClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String fullName = classPath + "/" + name.replaceAll("\\.", "/") + ".class";
            logger.debug("name: {}, fullName: {}", name, fullName);
            File file = new File(fullName);
            byte[] data = Files.readAllBytes(file.toPath());
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            logger.warn("e: {}", e);
            throw new ClassNotFoundException("Cannot findClass " + name, e.getCause());
        }
    }
}
