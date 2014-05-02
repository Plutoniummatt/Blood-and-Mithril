#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main()
{
  vec4 sampled = texture2D(u_texture, v_texCoords);
  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0 - sampled.a);
}