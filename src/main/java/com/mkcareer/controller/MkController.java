package com.mkcareer.controller;

import com.google.gson.Gson;
import com.mkcareer.dto.LoginRequest;
import com.mkcareer.entity.QuizQuestion;
import com.mkcareer.entity.Student;
import com.mkcareer.entity.StudentResult;
import com.mkcareer.entity.User;
import com.mkcareer.entity.WebsiteVisit;
import com.mkcareer.repository.QuizQuestionRepository;
import com.mkcareer.repository.StudentRepository;
import com.mkcareer.repository.StudentResultRepository;
import com.mkcareer.repository.UserRepository;
import com.mkcareer.repository.WebsiteVisitRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mkcarrer")
public class MkController {

	private final StudentRepository studentRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final StudentResultRepository studentResultRepository;
	private final WebsiteVisitRepository websiteVisitRepository;

	private final UserRepository userRepository;

	public MkController(StudentRepository studentRepository, QuizQuestionRepository quizQuestionRepository,
			StudentResultRepository studentResultRepository, UserRepository userRepository,
			WebsiteVisitRepository websiteVisitRepository) {

		this.studentRepository = studentRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.studentResultRepository = studentResultRepository;
		this.userRepository = userRepository;
		this.websiteVisitRepository = websiteVisitRepository;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {

		Optional<User> user = userRepository.findByUsernameAndPasswordAndStatus(request.getUsername(),
				request.getPassword(), 1);

		if (user.isEmpty()) {

			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Invalid username or password");

			return ResponseEntity.status(401).body(response);
		}

		User loggedUser = user.get();

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Login successful");
		response.put("username", loggedUser.getUsername());
		response.put("role", loggedUser.getRole());
		response.put("userId", loggedUser.getId());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerStudent(@RequestBody Student student) {
		Student savedStudent = studentRepository.save(student);
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Registration successful");
		response.put("studentId", savedStudent.getId());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/quiz/questions")
	public ResponseEntity<?> getQuizQuestions() {
		List<QuizQuestion> questions = quizQuestionRepository.findRandomQuestions();
		questions.forEach(question -> question.setCorrectAnswer(null));
		return ResponseEntity.ok(questions);
	}

	@PostMapping("/quiz/result")
	public ResponseEntity<?> saveQuizResult(@RequestBody Map<String, Object> request) {

		try {

			Object studentNameObject = request.get("studentName");
			Object studentMobileObject = request.get("studentMobile");
			Object answersObject = request.get("answers");

			if (studentNameObject == null || studentNameObject.toString().trim().isEmpty()) {

				return ResponseEntity.badRequest()
						.body(Map.of("success", false, "message", "Student name is required"));
			}

			if (!(answersObject instanceof List)) {

				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Answers are missing"));
			}

			String studentName = studentNameObject.toString().trim();

			String studentMobile = "";

			if (studentMobileObject != null) {
				studentMobile = studentMobileObject.toString().trim();
			}

			List<Map<String, Object>> answers = (List<Map<String, Object>>) answersObject;

			if (answers.isEmpty()) {

				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No answers received"));
			}

			Student student = new Student();

			student.setFullName(studentName);
			student.setPhone(studentMobile);
			student.setGrade("Mock Test");

			Student savedStudent = studentRepository.save(student);

			int totalQuestions = answers.size();
			int correctAnswers = 0;

			for (Map<String, Object> answer : answers) {

				Long questionId = Long.valueOf(answer.get("questionId").toString());

				String selectedAnswer = answer.get("selectedAnswer").toString();

				QuizQuestion question = quizQuestionRepository.findById(questionId)
						.orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

				if (question.getCorrectAnswer() != null
						&& question.getCorrectAnswer().equalsIgnoreCase(selectedAnswer)) {

					correctAnswers++;
				}
			}

			int wrongAnswers = totalQuestions - correctAnswers;

			int score = correctAnswers * 2;

			double percentage = totalQuestions == 0 ? 0 : ((double) correctAnswers / totalQuestions) * 100;

			StudentResult result = new StudentResult();

			result.setStudent(savedStudent);
			result.setTotalQuestions(totalQuestions);
			result.setCorrectAnswers(correctAnswers);
			result.setWrongAnswers(wrongAnswers);
			result.setScore(score);
			result.setPercentage(percentage);

			StudentResult savedResult = studentResultRepository.save(result);

			Map<String, Object> response = new HashMap<>();

			response.put("success", true);
			response.put("message", "Quiz result saved successfully");
			response.put("studentId", savedStudent.getId());
			response.put("studentName", savedStudent.getFullName());
			response.put("studentMobile", savedStudent.getPhone());
			response.put("resultId", savedResult.getId());
			response.put("totalQuestions", totalQuestions);
			response.put("correctAnswers", correctAnswers);
			response.put("wrongAnswers", wrongAnswers);
			response.put("score", score);
			response.put("percentage", percentage);

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
		}
	}

	@PostMapping("/visit")
	public ResponseEntity<?> saveVisit(@RequestParam(required = false) String pageUrl, HttpServletRequest request) {

		try {

			WebsiteVisit visit = new WebsiteVisit();

			visit.setPageUrl(pageUrl);

			String ipAddress = request.getHeader("X-Forwarded-For");

			if (ipAddress == null || ipAddress.isBlank()) {
				ipAddress = request.getRemoteAddr();
			}

			visit.setIpAddress(ipAddress);
			visit.setUserAgent(request.getHeader("User-Agent"));

			websiteVisitRepository.save(visit);

			return ResponseEntity.ok(Map.of("success", true, "message", "Visit recorded"));

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.internalServerError()
					.body(Map.of("success", false, "message", "Unable to save visit"));
		}
	}

	@GetMapping("/dashboard/summary")
	public ResponseEntity<?> getDashboardSummary() {

		Map<String, Object> response = new HashMap<>();

		response.put("students", studentRepository.count());

		response.put("mockQuestions", quizQuestionRepository.count());

		response.put("mockTests", studentResultRepository.count());

		response.put("totalVisits", websiteVisitRepository.countTotalVisits());

		response.put("uniqueVisitors", websiteVisitRepository.countUniqueVisitors());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/dashboard/students")
	public ResponseEntity<?> getStudents() {

		return ResponseEntity.ok(studentRepository.findAll());
	}

	@GetMapping("/dashboard/visits")
	public ResponseEntity<?> getVisits() {

		return ResponseEntity.ok(websiteVisitRepository.findAll());
	}

	@GetMapping("/dashboard/mock-results")
	public ResponseEntity<?> getMockResults() {

	    try {

	        List<StudentResult> results =
	                studentResultRepository.findAll();

	        List<Map<String, Object>> response =
	                new ArrayList<>();

	        for (StudentResult result : results) {

	            Map<String, Object> item =
	                    new HashMap<>();

	            item.put("id", result.getId());

	            if (result.getStudent() != null) {

	                item.put(
	                        "studentName",
	                        result.getStudent().getFullName()
	                );

	                item.put(
	                        "studentMobile",
	                        result.getStudent().getPhone()
	                );

	            } else {

	                item.put("studentName", "-");
	                item.put("studentMobile", "-");
	            }

	            item.put(
	                    "totalQuestions",
	                    result.getTotalQuestions()
	            );

	            item.put(
	                    "correctAnswers",
	                    result.getCorrectAnswers()
	            );

	            item.put(
	                    "wrongAnswers",
	                    result.getWrongAnswers()
	            );

	            item.put(
	                    "score",
	                    result.getScore()
	            );

	            item.put(
	                    "percentage",
	                    result.getPercentage()
	            );

	            item.put(
	                    "createdAt",
	                    result.getCreatedAt() != null
	                            ? result.getCreatedAt().toString()
	                            : null
	            );

	            response.add(item);
	        }

	        System.out.println(
	                "Mock results count: " + response.size()
	        );

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {

	        e.printStackTrace();

	        Map<String, Object> error =
	                new HashMap<>();

	        error.put("success", false);
	        error.put(
	                "message",
	                "Unable to load mock test results"
	        );

	        return ResponseEntity
	                .internalServerError()
	                .body(error);
	    }
	}

	
}
