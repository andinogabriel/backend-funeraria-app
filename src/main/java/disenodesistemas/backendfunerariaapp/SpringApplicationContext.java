package disenodesistemas.backendfunerariaapp;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    //Ponemos en nuestra constante el app context
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    //Hacemos esto porque en algunas clases no podemos usar beans ni autoWired
    public static Object getBean(final String beanName ) {
        return CONTEXT.getBean(beanName);
    }
}
