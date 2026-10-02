package com.technox.common.config;

import com.technox.category.entity.Category;
import com.technox.category.repository.CategoryRepository;
import com.technox.club.entity.Club;
import com.technox.club.repository.ClubRepository;
import com.technox.committee.entity.CommitteeMember;
import com.technox.committee.repository.CommitteeMemberRepository;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
import com.technox.faculty.entity.Faculty;
import com.technox.faculty.repository.FacultyRepository;
import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import com.technox.registration.repository.RegistrationRepository;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import com.technox.user.entity.Role;
import com.technox.user.entity.RoleType;
import com.technox.user.entity.User;
import com.technox.user.entity.UserStatus;
import com.technox.user.repository.RoleRepository;
import com.technox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CommitteeMemberRepository committeeMemberRepository;
    private final CategoryRepository categoryRepository;
    private final ClubRepository clubRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding database initial demo data...");

        Role adminRole = roleRepository.findByName(RoleType.ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.ADMIN).description("Platform Administrator").build()));
        Role facultyRole = roleRepository.findByName(RoleType.FACULTY).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.FACULTY).description("Faculty Coordinator").build()));
        Role committeeRole = roleRepository.findByName(RoleType.MANAGEMENT_COMMITTEE).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.MANAGEMENT_COMMITTEE).description("Student Committee Member").build()));
        Role studentRole = roleRepository.findByName(RoleType.STUDENT).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.STUDENT).description("College Student").build()));

        // Ensure categories exist
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Technical").description("Coding, Hackathons, Web Dev, AI/ML").colorCode("#3b82f6").build());
            categoryRepository.save(Category.builder().name("Cultural").description("Dance, Music, Drama, Fashion, Fine Arts").colorCode("#ec4899").build());
            categoryRepository.save(Category.builder().name("Workshops").description("Hands-on masterclasses and technical bootcamps").colorCode("#8b5cf6").build());
            categoryRepository.save(Category.builder().name("Sports").description("Cricket, Football, Basketball, Indoor tournaments").colorCode("#10b981").build());
            categoryRepository.save(Category.builder().name("Academic").description("Debates, Quiz, Paper Presentations").colorCode("#f59e0b").build());
            categoryRepository.save(Category.builder().name("CSR").description("Community engagement and social welfare").colorCode("#06b6d4").build());
        }

        // Ensure clubs exist
        if (clubRepository.count() == 0) {
            clubRepository.save(Club.builder()
                    .code("CODING_CLUB")
                    .name("Techno Coding Society")
                    .tagline("Code. Build. Innovate.")
                    .description("Official competitive programming and software engineering society.")
                    .facultyCoordinator("Dr. Vikram Malhotra")
                    .active(true)
                    .build());
        }

        // 1. Seed Admin User
        User adminUser = userRepository.findByEmail("admin@tgi.ac.in").orElseGet(() -> {
            User u = User.builder()
                    .uuid(UUID.randomUUID().toString())
                    .name("Dr. Rajeshwar Sen")
                    .email("admin@tgi.ac.in")
                    .mobile("+91 98765 00001")
                    .passwordHash(passwordEncoder.encode("Admin@TechnoX2026"))
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Collections.singleton(adminRole))
                    .build();
            return userRepository.save(u);
        });

        // 2. Seed Faculty User & Profile
        User facultyUser = userRepository.findByEmail("faculty@tgi.ac.in").orElseGet(() -> {
            User u = User.builder()
                    .uuid(UUID.randomUUID().toString())
                    .name("Dr. Vikram Malhotra")
                    .email("faculty@tgi.ac.in")
                    .mobile("+91 94150 11223")
                    .passwordHash(passwordEncoder.encode("Faculty@TechnoX2026"))
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Collections.singleton(facultyRole))
                    .build();
            return userRepository.save(u);
        });

        if (facultyRepository.findByUserId(facultyUser.getId()).isEmpty()) {
            Faculty f = Faculty.builder()
                    .user(facultyUser)
                    .facultyCode("FAC-CS-042")
                    .designation("Associate Professor & HOD")
                    .department("Computer Science & Engineering")
                    .specialization("Artificial Intelligence & Distributed Systems")
                    .officeRoom("Academic Block A, Room 304")
                    .build();
            facultyRepository.save(f);
        }

        // 3. Seed Committee User & Profile
        User committeeUser = userRepository.findByEmail("committee@tgi.ac.in").orElseGet(() -> {
            User u = User.builder()
                    .uuid(UUID.randomUUID().toString())
                    .name("Priya Verma")
                    .email("committee@tgi.ac.in")
                    .mobile("+91 91234 56789")
                    .passwordHash(passwordEncoder.encode("Committee@TechnoX2026"))
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Collections.singleton(committeeRole))
                    .build();
            return userRepository.save(u);
        });

        if (committeeMemberRepository.findByUserId(committeeUser.getId()).isEmpty()) {
            CommitteeMember cm = CommitteeMember.builder()
                    .user(committeeUser)
                    .committeeCode("ABHIVYAKTI")
                    .committeeName("Abhivyakti Cultural Council")
                    .roleTitle("Cultural Convener")
                    .department("Information Technology")
                    .build();
            committeeMemberRepository.save(cm);
        }

        // 4. Seed Student User & Profile
        User studentUser = userRepository.findByEmail("student@tgi.ac.in").orElseGet(() -> {
            User u = User.builder()
                    .uuid(UUID.randomUUID().toString())
                    .name("Rohan Sharma")
                    .email("student@tgi.ac.in")
                    .mobile("+91 88776 65544")
                    .passwordHash(passwordEncoder.encode("Student@TechnoX2026"))
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Collections.singleton(studentRole))
                    .build();
            return userRepository.save(u);
        });

        Student student = studentRepository.findByUserId(studentUser.getId()).orElseGet(() -> {
            Student s = Student.builder()
                    .user(studentUser)
                    .studentId("TGI2025BCA768")
                    .course("BCA")
                    .year("3rd Year")
                    .semester("5th Semester")
                    .department("Department of Computer Applications")
                    .bloodGroup("O+")
                    .address("Faizabad Road, Lucknow, Uttar Pradesh")
                    .qrCode("QR-TGI2025BCA768")
                    .build();
            return studentRepository.save(s);
        });

        // 5. Seed Events if none exist
        if (eventRepository.count() == 0) {
            List<Category> categories = categoryRepository.findAll();
            Category techCat = categories.stream().filter(c -> c.getName().equalsIgnoreCase("Technical")).findFirst().orElse(null);
            Category culturalCat = categories.stream().filter(c -> c.getName().equalsIgnoreCase("Cultural")).findFirst().orElse(null);
            Category workshopCat = categories.stream().filter(c -> c.getName().equalsIgnoreCase("Workshops")).findFirst().orElse(null);

            List<Club> clubs = clubRepository.findAll();
            Club codingClub = clubs.stream().filter(c -> c.getCode().equalsIgnoreCase("CODING_CLUB")).findFirst().orElse(null);

            if (techCat != null) {
                Event hackathon = Event.builder()
                        .uuid(UUID.randomUUID().toString())
                        .title("TechnoHacks 2026 — 24H National Hackathon")
                        .slug("technohacks-2026")
                        .description("The flagship 24-hour national hackathon of Techno Group of Institutions Lucknow. Build innovative AI, Web3, and HealthTech solutions.")
                        .category(techCat)
                        .club(codingClub)
                        .committeeCode("ABHIVYAKTI")
                        .organizer("Techno Coding Society & Innovation Cell")
                        .bannerUrl("https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80")
                        .eventDate(LocalDate.now().plusDays(15))
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .venue("Central Auditorium & AI Labs, TIHS Campus")
                        .capacity(120)
                        .registeredCount(1)
                        .waitlistCount(0)
                        .registrationDeadline(LocalDateTime.now().plusDays(12))
                        .eligibility("All B.Tech, BCA, MCA, and Polytechnic students")
                        .rules("Teams of 2-4 members. Original work only. Mentorship provided throughout.")
                        .requirements("Bring laptops, college ID, and valid digital pass.")
                        .status(EventStatus.PUBLISHED)
                        .createdBy(facultyUser)
                        .facultyCoordinator("Dr. Vikram Malhotra")
                        .committeeCoordinator("Priya Verma")
                        .approvedByUserId(facultyUser.getId())
                        .approvedAt(LocalDateTime.now())
                        .build();

                hackathon = eventRepository.save(hackathon);

                // Seed sample registration for demo student
                Registration sampleReg = Registration.builder()
                        .registrationId("TX-2026-784912")
                        .event(hackathon)
                        .student(student)
                        .status(RegistrationStatus.REGISTERED)
                        .registeredAt(LocalDateTime.now().minusDays(1))
                        .digitalPassId("PASS-TECHNO-001")
                        .qrToken("TX-QR-DEMO-STUDENT-PASS")
                        .passValidity("VALID")
                        .manualOverride(false)
                        .build();

                registrationRepository.save(sampleReg);
            }

            if (culturalCat != null) {
                Event culturalNight = Event.builder()
                        .uuid(UUID.randomUUID().toString())
                        .title("Sanskriti '26 — Inter-College Annual Cultural Fest")
                        .slug("sanskriti-2026")
                        .description("Two days of electric music performances, classical dance competitions, theatricals, and battle of the bands across colleges.")
                        .category(culturalCat)
                        .committeeCode("KIRAN")
                        .organizer("Kiran Cultural & Arts Council")
                        .bannerUrl("https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=80")
                        .eventDate(LocalDate.now().plusDays(25))
                        .startTime(LocalTime.of(16, 0))
                        .endTime(LocalTime.of(22, 0))
                        .venue("Main Open Air Amphitheatre, Faizabad Road Campus")
                        .capacity(500)
                        .registeredCount(0)
                        .waitlistCount(0)
                        .registrationDeadline(LocalDateTime.now().plusDays(20))
                        .eligibility("Open to all registered Techno students and alumni")
                        .status(EventStatus.PUBLISHED)
                        .createdBy(facultyUser)
                        .facultyCoordinator("Dr. Vikram Malhotra")
                        .build();

                eventRepository.save(culturalNight);
            }

            if (workshopCat != null) {
                Event aiWorkshop = Event.builder()
                        .uuid(UUID.randomUUID().toString())
                        .title("Full-Stack Cloud & GenAI Masterclass")
                        .slug("genai-masterclass")
                        .description("Hands-on masterclass building LLM agent workflows, containerized Spring Boot microservices, and modern frontend stacks.")
                        .category(workshopCat)
                        .committeeCode("OORJA")
                        .organizer("Oorja Technical Syndicate")
                        .bannerUrl("https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=1200&q=80")
                        .eventDate(LocalDate.now().plusDays(8))
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(14, 0))
                        .venue("Computer Lab 3, Block B")
                        .capacity(60)
                        .registeredCount(0)
                        .waitlistCount(0)
                        .registrationDeadline(LocalDateTime.now().plusDays(6))
                        .status(EventStatus.PUBLISHED)
                        .createdBy(facultyUser)
                        .build();

                eventRepository.save(aiWorkshop);
            }
        }

        log.info("Demo data verification and seeding complete.");
    }
}
