package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.util.EmailSender;
import com.epam.asset.tracking.util.RandomPasswordGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BusinessProviderServiceImplTest {

    BusinessProviderServiceImpl businessInstance;

    @Before
    public void setup() {
        businessInstance = mock(BusinessProviderServiceImpl.class);
        businessInstance.emailSender = mock(EmailSender.class);
        businessInstance.passwordGenerator = mock(RandomPasswordGenerator.class);
        businessInstance.repository = mock(BusinessProviderRepository.class);
        businessInstance.businessProvider = mock(BusinessProvider.class);

    }

    @Test
    public void shouldSendEmail() throws InvalidUserException {

        ArgumentCaptor valueCapture1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor valueCapture2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor valueCapture3 = ArgumentCaptor.forClass(String.class);

        BusinessProvider bp = mock(BusinessProvider.class);
        bp.setUsername("mmonraz");
        bp.setEmail("miguel_monraz@epam.com");
        bp.setName("Miguel");
        bp.setPassword("12345678");

        doReturn(Optional.of(bp)).when(businessInstance.repository).findByUsername("mmonraz");

        doNothing().when(businessInstance.emailSender).sendEmail(String.valueOf(valueCapture1.capture()), String.valueOf(valueCapture2.capture()), String.valueOf(valueCapture3.capture()));

        businessInstance.emailSender.sendEmail("miguel_monraz@epam.com", "Miguel", "abcdefghijk");

        when(businessInstance.passwordGenerator.generateNewPassword()).thenReturn("abcdefghijk");

        doReturn(bp).when(businessInstance).updatePassword(bp, "abcdefghijk");

        businessInstance.generatePasswordAndSendEmail("mmonraz");

        assertEquals("miguel_monraz@epam.com", valueCapture1.getValue());
        assertEquals("Miguel", valueCapture2.getValue());
        assertEquals("abcdefghijk", valueCapture3.getValue());
    }
}
