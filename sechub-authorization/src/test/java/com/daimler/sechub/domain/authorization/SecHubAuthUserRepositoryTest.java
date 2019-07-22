// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.authorization;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@SpringBootTest
@ContextConfiguration(classes = { AuthUserRepository.class, SecHubAuthUserRepositoryTest.SimpleTestConfiguration.class })
public class SecHubAuthUserRepositoryTest {

	@Autowired
	private AuthUserRepository sechubUserRepository;

	@Before
	public void before() throws Exception {

	}

	@Test
	public void is_able_to_store_user_with_role_superadmin_and_fetch_back() {

		/* prepare */
		AuthUser user = new AuthUser();
		user.userId="userid1";
		user.hashedApiToken="1234";
		user.roleSuperAdmin=true;

		/* execute */
		sechubUserRepository.save(user);
		
		/* test */
		List<AuthUser> users = sechubUserRepository.findAll();
		assertNotNull(users);
		assertFalse(users.isEmpty());
		assertEquals(1,users.size());
		AuthUser found = users.iterator().next();
		assertFalse(found.roleUser);
		assertTrue(found.roleSuperAdmin);
		assertEquals("userid1",found.getUserId());
		assertEquals("1234",found.getHashedApiToken());
	}

	@Test
	public void is_able_to_store_user()
			throws Exception {
		
		/* prepare */
		AuthUser user = new AuthUser();
		user.userId="userToSearchByName";
		user.hashedApiToken="1234";
		sechubUserRepository.save(user);
		
		/* execute */
		Optional<AuthUser> foundUser = sechubUserRepository.findByUserId("userToSearchByName");
		
		/* test */
		assertNotNull(foundUser);
		assertTrue(foundUser.isPresent());
		
	}
	
	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration {

	}

}
