package br.com.esplanada.web.rest;

import br.com.esplanada.ColegioEsplanadaApp;

import br.com.esplanada.domain.Professor;
import br.com.esplanada.repository.ProfessorRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ProfessorResource REST controller.
 *
 * @see ProfessorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ColegioEsplanadaApp.class)
public class ProfessorResourceIntTest {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_ANOTACAO = "AAAAAAAAAA";
    private static final String UPDATED_ANOTACAO = "BBBBBBBBBB";

    private static final String DEFAULT_ATIVIDADE = "AAAAAAAAAA";
    private static final String UPDATED_ATIVIDADE = "BBBBBBBBBB";

    private static final String DEFAULT_UPLOAD = "AAAAAAAAAA";
    private static final String UPDATED_UPLOAD = "BBBBBBBBBB";

    @Inject
    private ProfessorRepository professorRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restProfessorMockMvc;

    private Professor professor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ProfessorResource professorResource = new ProfessorResource();
        ReflectionTestUtils.setField(professorResource, "professorRepository", professorRepository);
        this.restProfessorMockMvc = MockMvcBuilders.standaloneSetup(professorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professor createEntity(EntityManager em) {
        Professor professor = new Professor()
                .nome(DEFAULT_NOME)
                .anotacao(DEFAULT_ANOTACAO)
                .atividade(DEFAULT_ATIVIDADE)
                .upload(DEFAULT_UPLOAD);
        return professor;
    }

    @Before
    public void initTest() {
        professor = createEntity(em);
    }

    @Test
    @Transactional
    public void createProfessor() throws Exception {
        int databaseSizeBeforeCreate = professorRepository.findAll().size();

        // Create the Professor

        restProfessorMockMvc.perform(post("/api/professors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(professor)))
                .andExpect(status().isCreated());

        // Validate the Professor in the database
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeCreate + 1);
        Professor testProfessor = professors.get(professors.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProfessor.getAnotacao()).isEqualTo(DEFAULT_ANOTACAO);
        assertThat(testProfessor.getAtividade()).isEqualTo(DEFAULT_ATIVIDADE);
        assertThat(testProfessor.getUpload()).isEqualTo(DEFAULT_UPLOAD);
    }

    @Test
    @Transactional
    public void getAllProfessors() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professors
        restProfessorMockMvc.perform(get("/api/professors?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(professor.getId().intValue())))
                .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME.toString())))
                .andExpect(jsonPath("$.[*].anotacao").value(hasItem(DEFAULT_ANOTACAO.toString())))
                .andExpect(jsonPath("$.[*].atividade").value(hasItem(DEFAULT_ATIVIDADE.toString())))
                .andExpect(jsonPath("$.[*].upload").value(hasItem(DEFAULT_UPLOAD.toString())));
    }

    @Test
    @Transactional
    public void getProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get the professor
        restProfessorMockMvc.perform(get("/api/professors/{id}", professor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(professor.getId().intValue()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME.toString()))
            .andExpect(jsonPath("$.anotacao").value(DEFAULT_ANOTACAO.toString()))
            .andExpect(jsonPath("$.atividade").value(DEFAULT_ATIVIDADE.toString()))
            .andExpect(jsonPath("$.upload").value(DEFAULT_UPLOAD.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingProfessor() throws Exception {
        // Get the professor
        restProfessorMockMvc.perform(get("/api/professors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();

        // Update the professor
        Professor updatedProfessor = professorRepository.findOne(professor.getId());
        updatedProfessor
                .nome(UPDATED_NOME)
                .anotacao(UPDATED_ANOTACAO)
                .atividade(UPDATED_ATIVIDADE)
                .upload(UPDATED_UPLOAD);

        restProfessorMockMvc.perform(put("/api/professors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedProfessor)))
                .andExpect(status().isOk());

        // Validate the Professor in the database
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeUpdate);
        Professor testProfessor = professors.get(professors.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProfessor.getAnotacao()).isEqualTo(UPDATED_ANOTACAO);
        assertThat(testProfessor.getAtividade()).isEqualTo(UPDATED_ATIVIDADE);
        assertThat(testProfessor.getUpload()).isEqualTo(UPDATED_UPLOAD);
    }

    @Test
    @Transactional
    public void deleteProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);
        int databaseSizeBeforeDelete = professorRepository.findAll().size();

        // Get the professor
        restProfessorMockMvc.perform(delete("/api/professors/{id}", professor.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeDelete - 1);
    }
}
