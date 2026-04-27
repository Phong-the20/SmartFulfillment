package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.entity.Role;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.repository.RoleRepository;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private AccountRepository accountRepo;
    @Autowired private RoleRepository roleRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");

        Account account = accountRepo.findByEmail(email);
        if (account == null) {
            account = new Account();
            account.setAccountId(UUID.randomUUID());
            account.setEmail(email);
            account.setUsername(email);
            account.setFullName(oauth2User.getAttribute("name"));
            account.setPassword_hash("");

            // Tìm quyền CUSTOMER trong DB
            Role customerRole = roleRepo.findById("CUSTOMER").orElse(null);
            if (customerRole != null) {
                account.getRoles().add(customerRole); // Gán quyền vào Set
            }

            accountRepo.save(account); // Spring sẽ tự động lưu vào 2 bảng Accounts và Account_Roles
        }
        return oauth2User;
    }
}