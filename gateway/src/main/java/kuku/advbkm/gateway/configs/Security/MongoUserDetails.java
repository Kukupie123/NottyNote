package kuku.advbkm.gateway.configs.Security;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

/**
 * Used by MongoUserDetailService to fetch user object from db service and store it as this object
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter //IMPORTANT !!!! Since we are using this as a response/request body as well we need valid getters and setters
@ToString
public class MongoUserDetails implements UserDetails {

    private String email;
    private String password;

    private String name;
    private String type;






    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (type == null) {
            //Probably means that we are using the object as a payload for other things rather than as UserDetail
            return Arrays.asList();
        }
        //Split by ,
        //Map each result into simpleGrantedAuth object and then convert the stream into collection
        String[] roleSplit = type.split(",");
        return Arrays.stream(roleSplit).map(s -> new SimpleGrantedAuthority(s)).toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
