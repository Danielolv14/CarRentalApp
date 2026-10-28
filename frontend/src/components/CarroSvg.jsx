/** Ilustração lateral de um carro (a mesma dos wireframes). */
export default function CarroSvg({ cor = '#2B59C3', largura = 190 }) {
  const altura = Math.round((largura * 90) / 220)
  return (
    <svg width={largura} height={altura} viewBox="0 0 220 90" aria-hidden="true">
      <ellipse cx="113" cy="85" rx="98" ry="4" fill="#15181E" opacity="0.14" />
      <path
        d="M14 62C14 52 20 48 34 46L62 42L88 24C94 20 100 18 110 18H150C160 18 168 22 174 28L188 42L200 45C208 47 212 52 212 60V66C212 69 210 70 207 70H196A20 20 0 0 0 156 70H74A20 20 0 0 0 34 70H18C15 70 14 68 14 66Z"
        fill={cor}
      />
      <path d="M70 43L92 28C96 25 100 24 106 24H126V43Z" fill="#DCE6F2" />
      <path d="M132 24H150C157 24 162 27 166 31L177 43H132Z" fill="#DCE6F2" />
      <rect x="15" y="51" width="10" height="4" rx="2" fill="#FFF3C4" />
      <rect x="203" y="51" width="8" height="4" rx="2" fill="#E5484D" />
      <rect x="98" y="47" width="12" height="2" rx="1" fill="#15181E" opacity="0.35" />
      <rect x="142" y="47" width="12" height="2" rx="1" fill="#15181E" opacity="0.35" />
      <circle cx="54" cy="70" r="15" fill="#15181E" />
      <circle cx="54" cy="70" r="6" fill="#C9CDD6" />
      <circle cx="176" cy="70" r="15" fill="#15181E" />
      <circle cx="176" cy="70" r="6" fill="#C9CDD6" />
    </svg>
  )
}
