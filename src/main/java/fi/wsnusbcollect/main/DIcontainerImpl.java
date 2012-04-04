/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.main;

/**
 *
 * @author ph4r05
 */
public class DIcontainerImpl implements DIcontainer {

    /**
     * Basic method will return initialized bean
     * @param string
     * @return
     * @throws BeansException 
     */
    @Override
    public Object getBean(String string) throws BeansException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getBean(String string, Class<T> type) throws BeansException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getBean(Class<T> type) throws BeansException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getBean(String string, Object[] os) throws BeansException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsBean(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSingleton(String string) throws NoSuchBeanDefinitionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isPrototype(String string) throws NoSuchBeanDefinitionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTypeMatch(String string, Class<?> type) throws NoSuchBeanDefinitionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<?> getType(String string) throws NoSuchBeanDefinitionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getAliases(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
