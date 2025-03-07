package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validator.TicketRequestValidator;

import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    private TicketServiceImpl ticketService;
    private TicketPaymentService mockPaymentService;
    private SeatReservationService mockSeatService;
    private TicketRequestValidator mockValidator;

    @Before
    public void setUp() {
        mockPaymentService = mock(TicketPaymentService.class);
        mockSeatService = mock(SeatReservationService.class);
        mockValidator = mock(TicketRequestValidator.class);
        ticketService = new TicketServiceImpl(mockPaymentService, mockSeatService, mockValidator);
    }

    // Successful purchase with valid request
    @Test
    public void shouldProcessValidPurchase() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        ticketService.purchaseTickets(1L, adultTicket, childTicket);

        verify(mockValidator).validate(1L, adultTicket, childTicket);
        verify(mockPaymentService).makePayment(1L, 2 * 25 + 1 * 15); // 50 + 15 = 65
        verify(mockSeatService).reserveSeat(1L, 3); // 2 Adults + 1 Child = 3 seats
    }

    // Ensures exception is thrown for invalid requests
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowExceptionForInvalidRequest() {
        doThrow(new InvalidPurchaseException("Invalid request")).when(mockValidator).validate(anyLong(), any());

        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));
    }

    // Ensure payment is never made if validation fails
    @Test
    public void shouldNotMakePaymentOrReserveSeatsWhenValidationFails() {
        doThrow(new InvalidPurchaseException("Invalid request")).when(mockValidator).validate(anyLong(), any());

        try {
            ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));
        } catch (InvalidPurchaseException ignored) {
        }

        verifyNoInteractions(mockPaymentService);
        verifyNoInteractions(mockSeatService);
    }

    // Infants should not be charged, only seats reserved
    @Test
    public void shouldNotChargeForInfants() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(1L, adultTicket, infantTicket);

        verify(mockPaymentService).makePayment(1L, 2 * 25); // Only adults = 50
        verify(mockSeatService).reserveSeat(1L, 2); // Infants don’t take a seat
    }
}
