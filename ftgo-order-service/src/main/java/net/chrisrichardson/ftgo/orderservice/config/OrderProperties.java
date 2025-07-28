package net.chrisrichardson.ftgo.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ftgo.order")
public class OrderProperties {
    private Delivery delivery = new Delivery();
    private Defaults defaults = new Defaults();

    public Delivery getDelivery() { 
        return delivery; 
    }
    
    public void setDelivery(Delivery delivery) { 
        this.delivery = delivery; 
    }
    
    public Defaults getDefaults() { 
        return defaults; 
    }
    
    public void setDefaults(Defaults defaults) { 
        this.defaults = defaults; 
    }

    public static class Delivery {
        private int windowMinutes = 30;
        
        public int getWindowMinutes() { 
            return windowMinutes; 
        }
        
        public void setWindowMinutes(int windowMinutes) { 
            this.windowMinutes = windowMinutes; 
        }
    }

    public static class Defaults {
        private long consumerId = 1511300065921L;
        
        public long getConsumerId() { 
            return consumerId; 
        }
        
        public void setConsumerId(long consumerId) { 
            this.consumerId = consumerId; 
        }
    }
}
