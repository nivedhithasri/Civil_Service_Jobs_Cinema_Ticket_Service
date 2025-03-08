package uk.gov.dwp.uc.pairtest.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TicketRequestValidatorTest {

    private TicketRequestValidator validator;

    private static final Long VALID_ACCOUNT_ID = 1L;
    private static final Long INVALID_ACCOUNT_ID_NULL = null;
    private static final Long INVALID_ACCOUNT_ID_ZERO = 0L;
    private static final int MAX_TICKETS_ALLOWED = 25;

    private final Long accountId;
    private final TicketTypeRequest[] ticketRequests;

    // Fix: Use varargs (TicketTypeRequest...)
    public TicketRequestValidatorTest(Long accountId, TicketTypeRequest... ticketRequests) {
        this.accountId = accountId;
        this.ticketRequests = ticketRequests;
    }

    @Before
    public void setUp() {
        validator = new TicketRequestValidator();
    }

    // Valid case: 2 Adults, 1 Child (should pass)
    @Test
    public void shouldPassForValidTicketRequest() {
        validator.validate(VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
        );
    }

    // Invalid cases - These should throw InvalidPurchaseException
    @Parameterized.Parameters(name = "{index}: Invalid Case - Account ID: {0}, Tickets: {1}")
    public static Collection<Object[]> invalidTicketRequests() {
        return Arrays.asList(new Object[][]{
                {INVALID_ACCOUNT_ID_NULL, new TicketTypeRequest[]{new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)}},
                {INVALID_ACCOUNT_ID_ZERO, new TicketTypeRequest[]{new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)}},
                {VALID_ACCOUNT_ID, new TicketTypeRequest[]{new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)}}, // No adult ticket
                {VALID_ACCOUNT_ID, new TicketTypeRequest[]{new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)}}, // No adult ticket
                {VALID_ACCOUNT_ID, new TicketTypeRequest[]{}}, // No tickets at all
                {VALID_ACCOUNT_ID, new TicketTypeRequest[]{new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -2)}}, // Negative ticket count
                {VALID_ACCOUNT_ID, new TicketTypeRequest[]{
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 8) // Exceeds limit (26 tickets)
                }}
        });
    }

    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForInvalidRequests() {
        validator.validate(accountId, ticketRequests);
    }
}
