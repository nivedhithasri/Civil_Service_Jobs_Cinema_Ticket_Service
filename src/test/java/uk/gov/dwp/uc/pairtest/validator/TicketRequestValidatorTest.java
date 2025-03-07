package uk.gov.dwp.uc.pairtest.validator;

import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketRequestValidatorTest {

    private TicketRequestValidator validator;

    @Before
    public void setUp() {
        validator = new TicketRequestValidator();
    }

    // Valid case: 2 Adult, 1 Child (should pass)
    @Test
    public void shouldPassForValidTicketRequest() {
        validator.validate(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
        );
    }

    // Invalid account ID (null or zero)
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForNullAccountId() {
        validator.validate(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForZeroAccountId() {
        validator.validate(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1));
    }

    // No Adult tickets but Child/Infant tickets present
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionWhenNoAdultTicketWithChild() {
        validator.validate(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        );
    }

    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionWhenNoAdultTicketWithInfant() {
        validator.validate(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );
    }

    // Exceeding ticket limit (more than 25)
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForExceedingTicketLimit() {
        validator.validate(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6)
        );
    }

    // Zero or negative tickets in request
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForZeroTickets() {
        validator.validate(1L);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForNegativeTicketCount() {
        validator.validate(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -2)
        );
    }
}
