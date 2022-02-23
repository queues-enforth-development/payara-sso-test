/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.test.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.FactoryFinder;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.inject.Named;
import net.bootsfaces.component.ComponentsEnum;

/**
 * A bean to initialize the renderers for Bootsfaces (as they are often not set by the container).
 * @author jimmc
 */
@Named
@SessionScoped
public class InitBootsfacesBean implements java.io.Serializable {
    
    /**
     * An error logger for us
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.system.initbootsfaces");

    /**
     * Default Faces Render Kit
     */
    private RenderKit defaultRenderKit = null;
    
    /**
     * Remember if we're initialized
     */
    private static boolean initialized = false;
    
    
    /**
     * Construct our bean
     */
    public InitBootsfacesBean() {
        
    }
    
    /**
     * Do other initialization work
     */
    @PostConstruct
    private void init() {
        
    }
    
    /**
     * Initialize renderers
     */
    public void initialize() {
        
        if (initialized) {
            LOGGER.log(Level.FINE, "Init Bootfaces: Skipping initializing: Already initialized.");
        } else {
            
            // Initialize our renderers
            LOGGER.log(Level.FINE, "Init Bootsfaces: Initializing renderers...");
            initializeBootsfacesRenderers();
            LOGGER.log(Level.FINE, "Init Bootsfaces: Initialization complete.");
            
            // Remember we're initialized
            initialized = true;
            
        }
        
    }

    /**
     * Register the Bootsfaces renderers
     */
    private void initializeBootsfacesRenderers() {
        
        // Loop through all of the Bootsfaces components
        for (ComponentsEnum value : ComponentsEnum.values()) {
            
            // Get the component class information
            String className;
            
            // switchComponent has wrong classpath in ComponentsEnum
            if (value.name().equals("switchComponent")) {
                // Use correct qualified name
                className = "net.bootsfaces.component.switchComponent.Switch";
            } else {
                // Otherwise, use specified value
                className = value.classname();
            }
            
            // See if this is an internal reference
            if (className.contains("Internal")) {
                LOGGER.log(Level.FINER, "Init Bootsfaces: Skipping internal component: {0}", className);
            } else {
                
                LOGGER.log(Level.FINER, "Init Bootsfaces: Processing component: {0}", className);
                
                try {
                    
                    // See if we can instantiate the class
                    Class componentClass = Class.forName(className);
                    Class<UIComponentBase> baseClass = componentClass.asSubclass(UIComponentBase.class);
                    UIComponentBase component = baseClass.newInstance();
                    String rendererFamily = component.getFamily();
                    String rendererType = component.getRendererType();

                    // Determine the renderer class name
                    String simpleName = componentClass.getSimpleName();
                    String rendererClassName;
                    switch (simpleName) {
                        case "NavCommandLink":
                            // Shares same renderer with NavLink
                            rendererClassName = "net.bootsfaces.component.navLink.NavLinkRenderer";
                            break;
                        default:
                            // We have to guess at the name of the renderer
                            rendererClassName = className + "Renderer";
                            
                    }
                    
                    // Look up the renderer
                    Class rendererSuperclass = Class.forName(rendererClassName);
                    Class<Renderer> rendererClass = rendererSuperclass.asSubclass(Renderer.class);
                    Renderer renderer = rendererClass.newInstance();
                    LOGGER.log(Level.FINER, "Init Bootsfaces: Registering renderer: {0}/{1}", new Object[]{rendererFamily, rendererType});
                    addRenderer(rendererFamily, rendererType, renderer);
                    
                } catch (Throwable e) {
                    LOGGER.log(Level.FINER, "Init Bootsfaces: Unable to register renderer for component: " + className, e);
                }
                
            }

        }
        
    }
    
    /**
     * Get the default FACES renderer
     * @return 
     */
    private RenderKit getDefaultRenderKit() {
        if(defaultRenderKit == null) {
            RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            defaultRenderKit = renderKitFactory.getRenderKit(facesContext, RenderKitFactory.HTML_BASIC_RENDER_KIT);
        }

        return defaultRenderKit;
    }
    
    /**
     * Add a renderer
     * @param family
     * @param rendererType
     * @param renderer 
     */
    public void addRenderer(String family, String rendererType, Renderer renderer) {
        getDefaultRenderKit().addRenderer(family, rendererType, renderer);
    }
    
    /**
     * Get our version #
     * @return 
     */
    public String getVersion() {
        return "Version 1.00.0000";
    }

}
