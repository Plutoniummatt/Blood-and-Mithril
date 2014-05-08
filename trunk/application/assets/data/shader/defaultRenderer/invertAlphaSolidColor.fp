#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 c;

void main()
{
  vec4 a = texture2D(u_texture, v_texCoords);
  gl_FragColor = a + vec4(c.rgb * (1.0 - a.a), 1.0);
}