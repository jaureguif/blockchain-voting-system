package com.epam.asset.tracking.util;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EmailSenderTest {

    @Test
    public void shouldSendEmail(){

        EmailSender emailSenderInstance = mock(EmailSender.class);
        verify(emailSenderInstance, times(1)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
