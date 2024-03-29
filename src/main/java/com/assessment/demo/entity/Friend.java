    package com.assessment.demo.entity;

    import com.assessment.demo.entity.Enum.RequestStatus;
    import com.assessment.demo.entity.base.BaseEntity;
    import com.assessment.demo.entity.base.FriendId;
    import jakarta.persistence.*;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.Getter;

    import java.util.UUID;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Entity
    @Table(name = "Friend")
    //@IdClass(FriendId.class)
    public class Friend extends BaseEntity {
        // Primary key
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "friendId", columnDefinition = "BINARY(16)")
        private UUID id;

        // Foreign keys
        @ManyToOne
        @JoinColumn(name = "senderId")
        private User sender;

        @ManyToOne
        @JoinColumn(name = "receiverId")
        private User receiver;

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        private RequestStatus status;

        public Friend() {
            super();
        }

        // Constructors
        public Friend(User sender, User receiver) {
            this();
            this.id = UUID.randomUUID();
            this.sender = sender;
            this.receiver = receiver;
            this.status = RequestStatus.PENDING;        // Friend request created, so the default status will be PENDING
        }

        // Methods for update friend status
        public void updateStatus(RequestStatus requestStatus) {
            this.status = requestStatus;
            this.updateDate();
        }
    }

