package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.validator.TicketRequestValidator;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {

    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private final TicketRequestValidator ticketRequestValidator;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService,
                             TicketRequestValidator ticketRequestValidator) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.ticketRequestValidator = ticketRequestValidator;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        ticketRequestValidator.validate(accountId, ticketTypeRequests);

        int totalAmount = calculateAmount(ticketTypeRequests);
        int totalSeats = calculateSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private int calculateAmount(TicketTypeRequest... requests) {
        int total = 0;
        for (TicketTypeRequest request : requests) {
            switch (request.getTicketType()) {
                case ADULT:
                    total += request.getNoOfTickets() * ADULT_PRICE;
                    break;
                case CHILD:
                    total += request.getNoOfTickets() * CHILD_PRICE;
                    break;
                case INFANT:
                    // Infants do not require payment
                    break;
            }
        }
        return total;
    }

    private int calculateSeats(TicketTypeRequest... requests) {
        return Arrays.stream(requests)
                .filter(request -> request.getTicketType() != TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }
}
