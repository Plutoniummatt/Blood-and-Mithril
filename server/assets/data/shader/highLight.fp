#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float alpha;

void main()
{
  gl_FragColor = texture2D(u_texture, v_texCoords) * (vec4(alpha, alpha, alpha, 1) + vec4(0.2, 0.2, 0.2, 0));
}