# Rules

 * Date format in ISO8610, see dto/UserBasic.java for example annotation
 * PUT/POST input json class name should end with `Entry`
 * every request mapping method should include ClientAppUserAccessToken, and 
   annotated with @RequiredScope 
 * 
 
# Notice

 * nginx
  
  `/v1` prefix has special configuration in nginx, change or add more prefix please
   review nginx settings in ansible