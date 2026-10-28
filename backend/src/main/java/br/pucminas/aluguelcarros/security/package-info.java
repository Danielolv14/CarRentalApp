/**
 * Autenticação com usuário e senha (Spring Security + BCrypt). Todas as rotas exigem login,
 * menos o próprio login e o cadastro. A permissão de cada rota depende do {@code Perfil} (CLIENTE ou AGENTE).
 */
package br.pucminas.aluguelcarros.security;
